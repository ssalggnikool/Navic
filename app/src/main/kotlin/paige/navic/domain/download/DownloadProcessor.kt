/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.download

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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.DownloadDao
import paige.navic.database.dao.LyricDao
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.database.entity.LyricEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.StorageManager
import paige.navic.domain.model.DomainSong
import paige.navic.domain.repository.LyricsRepository
import paige.navic.util.Logger
import paige.navic.util.network.NavicularDnsResolver
import paige.navic.util.network.createHttpClientWithPreferences

class DownloadCancelledException : Exception("Download was cancelled")

class DownloadProcessor(
	private val context: Context,
	private val imageLoader: ImageLoader,
	private val downloadDao: DownloadDao,
	private val albumDao: AlbumDao,
	private val songDao: SongDao,
	private val storageManager: StorageManager,
	private val lyricsRepository: LyricsRepository,
	private val lyricDao: LyricDao,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager,
	dnsResolver: NavicularDnsResolver
) {
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

	suspend fun download(songId: String, onProgress: (suspend (Float) -> Unit)? = null) {
		requireQueued(songId)

		val song = resolveSong(songId)

		cacheCoverArt(song.coverArtId)
		cacheAlbumCoverArt(song.albumId)
		cacheLyrics(song)
		downloadAudioFile(song, onProgress)
	}

	private suspend fun resolveSong(songId: String): DomainSong {
		songDao.getSongById(songId)?.let { return it.toDomainModel() }

		val remote = sessionManager.api.getSong(songId).toEntity().toDomainModel()
		songDao.insertSong(remote.toEntity())
		return remote
	}

	private suspend fun requireQueued(songId: String) {
		currentCoroutineContext().ensureActive()

		if (downloadDao.getDownloadById(songId)?.status != DownloadStatus.DOWNLOADING) {
			throw DownloadCancelledException()
		}
	}

	private suspend fun cacheCoverArt(coverId: String?) {
		if (coverId == null) return

		try {
			Logger.i("DownloadProcessor", "caching cover art for $coverId")
			val coverArtUrl = sessionManager.getCoverArtUrl(coverId)

			val imageRequest = ImageRequest.Builder(context)
				.data(coverArtUrl)
				.size(Size.ORIGINAL)
				.memoryCacheKey(coverId)
				.diskCacheKey(coverId)
				.diskCachePolicy(CachePolicy.ENABLED)
				.memoryCachePolicy(CachePolicy.DISABLED)
				.build()

			imageLoader.execute(imageRequest)
			Logger.i("DownloadProcessor", "cached cover art for $coverId")
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadProcessor", "Failed to cache cover art for $coverId", e)
		}
	}

	private suspend fun cacheAlbumCoverArt(albumId: String?) {
		if (albumId == null) return

		try {
			val albumWithSongs = albumDao.getAlbumById(albumId)
			val albumCoverId = albumWithSongs?.album?.coverArtId

			if (albumCoverId != null) {
				Logger.i(
					"DownloadProcessor",
					"Found album cover $albumCoverId for album $albumId"
				)
				cacheCoverArt(albumCoverId)
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e(
				"DownloadProcessor",
				"Failed to cache album cover art for album $albumId",
				e
			)
		}
	}

	private suspend fun cacheLyrics(song: DomainSong) {
		Logger.i("DownloadProcessor", "caching lyrics for ${song.id}")
		try {
			val lyricsResult = lyricsRepository.fetchLyrics(song)
			if (lyricsResult?.rawContent != null) {
				lyricDao.insertLyrics(
					LyricEntity(
						songId = song.id,
						rawContent = lyricsResult.rawContent,
						providerName = lyricsResult.providerName
					)
				)
				Logger.i("DownloadProcessor", "cached lyrics for ${song.id}")
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadProcessor", "Failed to cache lyrics for ${song.id}", e)
		}
	}

	private suspend fun downloadAudioFile(
		song: DomainSong,
		onProgress: (suspend (Float) -> Unit)?
	) {
		var lastProgress = 0f

		val bitrate = if (preferenceManager.isAdvancedDownloadTranscodingActive) {
			preferenceManager.customDownloadMaxBitrateWifi
		} else {
			preferenceManager.downloadQualityWifi.bitrate
		}
		val container = if (preferenceManager.isAdvancedDownloadTranscodingActive) {
			preferenceManager.customDownloadFormatWifi
		} else {
			preferenceManager.downloadQualityWifi.container
		}

		val extension = container?.takeIf { it.isNotBlank() } ?: song.fileExtension ?: "bin"
		val finalPath = storageManager.getDownloadPath(
			song.id,
			song.albumId ?: song.id,
			extension
		)
		val tempPath = storageManager.getTempDownloadPath(finalPath)

		storageManager.deleteTempFile(finalPath)

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
				val progress = contentLength
					?.let { (bytesSentTotal.toDouble() / it).toFloat() }
					?: Float.MAX_VALUE

				if (progress - lastProgress >= 0.01f || progress == 1f) {
					lastProgress = progress
					requireQueued(song.id)
					Logger.i("DownloadProcessor", "downloading ${song.id} $progress")

					downloadDao.updateProgress(
						song.id,
						DownloadStatus.DOWNLOADING,
						progress
					)
					onProgress?.invoke(progress)
				}
			}
		}

		try {
			request.execute { response ->
				Logger.i("DownloadProcessor", "writing download for ${song.id}")
				storageManager.saveFile(tempPath, response.bodyAsChannel())
				Logger.i("DownloadProcessor", "wrote download for ${song.id}")
			}

			requireQueued(song.id)

			if (!storageManager.commitFile(tempPath, finalPath)) {
				throw IllegalStateException("Could not move download into place for ${song.id}")
			}

			downloadDao.insertDownload(
				DownloadEntity(
					song.id,
					DownloadStatus.DOWNLOADED,
					1f,
					finalPath
				)
			)
		} catch (e: Exception) {
			storageManager.deleteTempFile(finalPath)
			throw e
		}
	}
}
