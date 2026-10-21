/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size
import dev.zt64.subsonic.api.model.SubsonicException
import dev.zt64.subsonic.api.model.SubsonicResponse
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import paige.navic.R
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.DownloadDao
import paige.navic.database.dao.LyricDao
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.database.entity.LyricEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainSong
import paige.navic.domain.repository.LyricsRepository
import paige.navic.util.Logger
import paige.navic.util.network.NavicularDnsResolver
import paige.navic.util.network.createHttpClientWithPreferences
import coil3.PlatformContext as CoilPlatformContext

class DownloadManager(
	private val coilPlatformContext: CoilPlatformContext,
	private val imageLoader: ImageLoader,
	private val downloadDao: DownloadDao,
	private val albumDao: AlbumDao,
	private val songDao: SongDao,
	private val storageManager: StorageManager,
	private val lyricsRepository: LyricsRepository,
	private val lyricDao: LyricDao,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager,
	private val connectivityManager: ConnectivityManager,
	private val notificationManager: NotificationManager,
	dnsResolver: NavicularDnsResolver,
	private val context: Context
) {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val client = createHttpClientWithPreferences(preferenceManager, dnsResolver).apply {
		config {
			HttpResponseValidator {
				validateResponse { response ->
					when (response.status.value) {
						in 400..499 -> throw ClientRequestException(
							response,
							cachedResponseText = response.bodyAsText()
						)
						in 500..599 -> throw ServerResponseException(
							response,
							cachedResponseText = response.bodyAsText()
						)
					}

					val contentType = response.headers["content-type"]

					if (contentType == ContentType.Application.Json.contentType) {
						try {
							val subsonicResponse =
								Json.decodeFromString<SubsonicResponse<Any>>(response.bodyAsText())

							if (subsonicResponse is SubsonicResponse.Error) {
								// something has gone wrong with request, throw an error immediately
								throw SubsonicException(
									subsonicResponse.error.message,
									subsonicResponse.error.code
								)
							}
						} catch (e: Exception) {
							if (e is SubsonicException) throw e
							// probably not our business, let something else handle the exception
						}
					}
				}
			}
		}
	}

	private val activeDownloadsMutex = Mutex()
	private val activeDownloads = mutableMapOf<String, Job>()
	private val downloadSemaphore =
		Semaphore(10)// idk a good number, maybe u should be able to choose

	private val downloadProgressMutex = Mutex()
	private var totalSongsToDownload = 0
	private var completedSongsDownloaded = 0

	private fun updateDownloadNotification() {
		if (totalSongsToDownload == 0) return

		if (completedSongsDownloaded >= totalSongsToDownload) {
			notificationManager.cancelNotification(NotificationIds.DOWNLOAD_LIBRARY)
			totalSongsToDownload = 0
			completedSongsDownloaded = 0
		} else {
			val progress = completedSongsDownloaded.toFloat() / totalSongsToDownload.toFloat()
			val titleStr = context.resources.getString(R.string.info_status_downloading)
			val msgStr = "$completedSongsDownloaded / $totalSongsToDownload"

			notificationManager.showProgressNotification(
				id = NotificationIds.DOWNLOAD_LIBRARY,
				title = "$titleStr (${(progress * 100).toInt()}%)",
				message = msgStr,
				progress = progress,
				indeterminate = false
			)
		}
	}

	private var libraryDownloadJob: Job? = null

	val allDownloads = downloadDao.getAllDownloads().map { it.toImmutableList() }
	val downloadCount = downloadDao.getDownloadsCount()
	val downloadSize = allDownloads.map { downloads ->
		downloads
			.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
			.sumOf { storageManager.getFileSize(it.filePath!!) }
	}

	val downloadedSongs: StateFlow<Map<String, String>>
		field = MutableStateFlow(emptyMap())

	val isDownloadingLibrary: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val libraryDownloadProgress: StateFlow<Float>
		field = MutableStateFlow(0f)

	init {
		scope.launch {
			allDownloads.collectLatest { downloads ->
				downloadedSongs.value = downloads
					.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
					.associate { it.songId to it.filePath!! }
			}
		}
	}

	fun getDownloadedFilePath(songId: String): String? {
		return downloadedSongs.value[songId]
	}

	fun downloadSong(songId: String): Job {
		return downloadSongInternal(songId, incrementCounter = true)
	}

	private fun downloadSongInternal(songId: String, incrementCounter: Boolean): Job {
		val job = scope.launch(Dispatchers.IO) {
			val alreadyActive =
				activeDownloadsMutex.withLock { activeDownloads.containsKey(songId) }
			if (alreadyActive || isDownloaded(songId)) return@launch

			if (incrementCounter) {
				downloadProgressMutex.withLock {
					if (totalSongsToDownload == 0) {
						completedSongsDownloaded = 0
					}
					totalSongsToDownload++
					updateDownloadNotification()
				}
			}

			try {
				activeDownloadsMutex.withLock { activeDownloads[songId] = coroutineContext[Job]!! }

				downloadSemaphore.withPermit {
					executeDownloadProcess(songId)
				}

				downloadProgressMutex.withLock {
					completedSongsDownloaded++
					updateDownloadNotification()
				}
			} catch (e: Exception) {
				downloadProgressMutex.withLock {
					completedSongsDownloaded++
					updateDownloadNotification()
				}
				throw e
			} finally {
				activeDownloadsMutex.withLock { activeDownloads.remove(songId) }
			}
		}
		return job
	}

	suspend fun downloadCollection(songIds: Collection<String>) {
		songIds
			.filter { !isDownloaded(it) }
			.forEach { downloadSong(it) }
	}

	// TODO: rewrite this turd, preferably using flows. what the fuck is this
	fun downloadEntireLibrary() {
		if (isDownloadingLibrary.value) return

		libraryDownloadJob = scope.launch(Dispatchers.IO) {
			try {
				isDownloadingLibrary.value = true
				libraryDownloadProgress.value = 0f

				val songIds = songDao.getAllSongIds()
				val songsToDownload = songIds.filter { !isDownloaded(it) }
				val totalToDownload = songsToDownload.size

				if (totalToDownload == 0) {
					isDownloadingLibrary.value = false
					libraryDownloadProgress.value = 1f
					return@launch
				}

				downloadProgressMutex.withLock {
					if (totalSongsToDownload == 0) {
						completedSongsDownloaded = 0
					}
					totalSongsToDownload += totalToDownload
					updateDownloadNotification()
				}

				val downloadQueue = Channel<String>(Channel.UNLIMITED)
				songsToDownload.forEach { downloadQueue.trySend(it) }
				downloadQueue.close()

				var processedCount = 0
				val progressMutex = Mutex()

				val workers = List(10) {
					launch {
						for (songId in downloadQueue) {
							downloadSongInternal(songId, incrementCounter = false).join()

							progressMutex.withLock {
								processedCount++
								val progress = processedCount.toFloat() / totalToDownload.toFloat()
								libraryDownloadProgress.value = progress
							}
						}
					}
				}

				workers.joinAll()
				isDownloadingLibrary.value = false

			} catch (_: CancellationException) {
				isDownloadingLibrary.value = false
				libraryDownloadProgress.value = 0f
			}
		}
	}

	fun cancelAllActiveDownloads() {
		libraryDownloadJob?.cancel()
		libraryDownloadJob = null
		isDownloadingLibrary.value = false
		libraryDownloadProgress.value = 0f

		scope.launch(Dispatchers.IO) {
			downloadProgressMutex.withLock {
				totalSongsToDownload = 0
				completedSongsDownloaded = 0
				notificationManager.cancelNotification(NotificationIds.DOWNLOAD_LIBRARY)
			}

			val jobsToCancel = activeDownloadsMutex.withLock {
				val copy = activeDownloads.toMap()
				activeDownloads.clear()
				copy
			}

			jobsToCancel.forEach { (songId, job) ->
				job.cancel()
				val existing = downloadDao.getDownloadById(songId)
				if (existing?.status == DownloadStatus.DOWNLOADING) {
					downloadDao.deleteDownload(songId)
				}
			}
		}
	}

	fun cancelDownload(songId: String) {
		scope.launch(Dispatchers.IO) {
			activeDownloadsMutex.withLock {
				activeDownloads[songId]?.cancel()
				activeDownloads.remove(songId)
			}

			val existing = downloadDao.getDownloadById(songId)
			if (existing?.status == DownloadStatus.DOWNLOADING
				|| existing?.status == DownloadStatus.FAILED
			) {
				downloadDao.deleteDownload(songId)
			}
		}
	}

	fun cancelCollectionDownload(songIds: Collection<String>) {
		songIds.forEach { songId ->
			cancelDownload(songId)
		}
	}

	fun deleteDownload(songId: String) {
		cancelDownload(songId)
		scope.launch {
			val download = downloadDao.getDownloadById(songId)
			download?.filePath?.let { storageManager.deleteFile(it) }
			downloadDao.deleteDownload(songId)
		}
	}

	fun deleteDownloadedCollection(songIds: Collection<String>) {
		songIds.forEach { songId ->
			deleteDownload(songId)
		}
	}

	suspend fun isDownloaded(songId: String): Boolean {
		return downloadDao.getDownloadById(songId)?.status == DownloadStatus.DOWNLOADED
	}

	fun getCollectionDownloadStatus(songIds: List<String>): Flow<DownloadStatus> {
		return allDownloads.map { downloads ->
			val collectionDownloads = downloads.filter { it.songId in songIds }
			when {
				collectionDownloads.isEmpty() -> DownloadStatus.NOT_DOWNLOADED
				collectionDownloads.any { it.status == DownloadStatus.DOWNLOADING } -> DownloadStatus.DOWNLOADING
				collectionDownloads.any { it.status == DownloadStatus.FAILED } -> DownloadStatus.FAILED
				(collectionDownloads.size == songIds.size &&
					collectionDownloads.all { it.status == DownloadStatus.DOWNLOADED })
					-> DownloadStatus.DOWNLOADED

				else -> DownloadStatus.NOT_DOWNLOADED
			}
		}
	}

	fun clearAllDownloads() {
		scope.launch(Dispatchers.IO) {
			cancelAllActiveDownloads()
			storageManager.clearDownloads()
			downloadDao.clearAllDownloads()
			Logger.i("DownloadManager", "cleared all downloads")
		}
	}

	private suspend fun executeDownloadProcess(songId: String) {
		try {
			Logger.i("DownloadManager", "beginning download for $songId")
			downloadDao.insertDownload(DownloadEntity(songId, DownloadStatus.DOWNLOADING, 0f))

			val song = songDao.getSongById(songId)?.toDomainModel()
			if (song != null) {
				cacheCoverArt(song.coverArtId)
				cacheAlbumCoverArt(song.albumId) //Maybe use a Dao function to retrieve these??
				cacheLyrics(songId)
				downloadAudioFile(song)
			} else {
				Logger.e("DownloadManager", "Song $songId not found for download")
				downloadDao.insertDownload(DownloadEntity(songId, DownloadStatus.FAILED, 0f))
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to download song $songId", e)
			downloadDao.insertDownload(DownloadEntity(songId, DownloadStatus.FAILED, 0f))
		} finally {
			activeDownloadsMutex.withLock {
				activeDownloads.remove(songId)
			}
		}
	}

	private suspend fun cacheCoverArt(coverId: String?) {
		if (coverId == null) return

		Logger.i("DownloadManager", "caching cover art for $coverId")
		val coverArtUrl = sessionManager.getCoverArtUrl(coverId)

		val imageRequest = ImageRequest.Builder(coilPlatformContext)
			.data(coverArtUrl)
			.size(Size.ORIGINAL)
			.memoryCacheKey(coverId)
			.diskCacheKey(coverId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.DISABLED)
			.build()

		imageLoader.execute(imageRequest)
		Logger.i("DownloadManager", "cached cover art for $coverId")
	}

	private suspend fun cacheAlbumCoverArt(albumId: String?) {
		if (albumId == null) return

		try {
			val albumWithSongs = albumDao.getAlbumById(albumId)
			val albumCoverId = albumWithSongs?.album?.coverArtId

			if (albumCoverId != null) {
				Logger.i("DownloadManager", "Found album cover $albumCoverId for album $albumId")
				cacheCoverArt(albumCoverId)
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to cache album cover art for album $albumId", e)
		}
	}

	private suspend fun cacheLyrics(songId: String) {
		Logger.i("DownloadManager", "caching lyrics for $songId")
		try {
			val lyricsResult = lyricsRepository.fetchLyrics(songId)
			if (lyricsResult?.rawContent != null) {
				lyricDao.insertLyrics(
					LyricEntity(
						songId = songId,
						rawContent = lyricsResult.rawContent,
						providerName = lyricsResult.providerName
					)
				)
				Logger.i("DownloadManager", "cached lyrics for $songId")
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to cache lyrics for $songId", e)
		}
	}

	private suspend fun downloadAudioFile(song: DomainSong) {
		var lastProgress = 0f
		var progressJob: Job? = null

		val isCellular = connectivityManager.isCellular.value
		val bitrate = if (preferenceManager.isAdvancedDownloadTranscodingActive) {
			if (isCellular) preferenceManager.customDownloadMaxBitrateCellular else preferenceManager.customDownloadMaxBitrateWifi
		} else {
			val quality = if (isCellular) preferenceManager.downloadQualityCellular else preferenceManager.downloadQualityWifi
			quality.bitrate
		}
		val container = if (preferenceManager.isAdvancedDownloadTranscodingActive) {
			if (isCellular) preferenceManager.customDownloadFormatCellular else preferenceManager.customDownloadFormatWifi
		} else {
			val quality = if (isCellular) preferenceManager.downloadQualityCellular else preferenceManager.downloadQualityWifi
			quality.container
		}

		val extension = container?.takeIf { it.isNotBlank() } ?: song.fileExtension

		val request = client.prepareRequest(
			sessionManager.api.getStreamUrl(
				id = song.id,
				maxBitRate = bitrate,
				format = container?.takeIf { it.isNotBlank() },
				// if this is true u get "stream was reset: INTERNAL_ERROR" for some reason
				estimateContentLength = false
			)
		) {
			method = HttpMethod.Get
			onDownload { bytesSentTotal, contentLength ->
				if (contentLength != null && contentLength > 0L) {
					val progress = (bytesSentTotal.toDouble() / contentLength).toFloat()
					if (progress - lastProgress >= 0.01f || progress == 1f) {
						lastProgress = progress
						Logger.i("DownloadManager", "downloading ${song.id} $progress")

						progressJob?.cancel()

						progressJob = scope.launch {
							downloadDao.updateProgress(
								song.id,
								DownloadStatus.DOWNLOADING,
								progress
							)
						}
					}
				} else {
					Logger.i("DownloadManager", "downloaded ${song.id}")
				}
			}
		}

		request.execute { response ->
			Logger.i("DownloadManager", "writing download for ${song.id}")
			val path = storageManager.getDownloadPath(
				song.id,
				extension ?: "mp3" // TODO: idk how to handle this being null lol
			)
			storageManager.saveFile(path, response.bodyAsChannel())
			Logger.i("DownloadManager", "wrote download for ${song.id}")

			progressJob?.cancel()

			downloadDao.insertDownload(
				DownloadEntity(
					song.id,
					DownloadStatus.DOWNLOADED,
					1f,
					path
				)
			)
		}
	}
}
