/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import paige.navic.database.dao.DownloadDao
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.domain.manager.runtime.NotificationIds
import paige.navic.domain.manager.runtime.NotificationManager
import paige.navic.domain.manager.runtime.StorageManager
import paige.navic.domain.model.settings.DownloadStorage
import paige.navic.util.Logger
import paige.navic.work.DownloadMigrationWorker
import paige.navic.work.DownloadWorker
import paige.navic.work.WorkKeys
import paige.navic.work.WorkNames
import java.io.File
import java.util.concurrent.TimeUnit

class DownloadManager(
	private val downloadDao: DownloadDao,
	private val songDao: SongDao,
	private val storageManager: StorageManager,
	private val notificationManager: NotificationManager,
	private val sessionManager: SessionManager,
	context: Context
) {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val workManager = WorkManager.getInstance(context)

	val allDownloads = downloadDao.getAllDownloads().map { it.toImmutableList() }
	val downloadCount = downloadDao.getDownloadsCount()
	val downloadSize = downloadDao.getDownloadedFilePathsFlow().map { paths ->
		paths.sumOf { storageManager.getFileSize(it) }
	}.flowOn(Dispatchers.IO)

	val downloadedSongs: StateFlow<Map<String, String>>
		field = MutableStateFlow(emptyMap())

	val isDownloadingLibrary: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val libraryDownloadProgress: StateFlow<Float>
		field = MutableStateFlow(0f)

	init {
		scope.launch {
			downloadDao.getDownloadedEntitiesFlow().collectLatest { downloads ->
				downloadedSongs.value = downloads
					.associate { it.songId to it.filePath!! }
			}
		}

		scope.launch {
			workManager.getWorkInfosForUniqueWorkFlow(WorkNames.DOWNLOAD_QUEUE)
				.collectLatest { infos ->
					val running = infos.lastOrNull { it.state == WorkInfo.State.RUNNING }
					val pending = infos.any {
						it.state == WorkInfo.State.ENQUEUED ||
							it.state == WorkInfo.State.BLOCKED
					}

					if (running != null) {
						libraryDownloadProgress.value = running.progress
							.getFloat(WorkKeys.PROGRESS, 0f)
					} else if (!pending && infos.isNotEmpty()) {
						isDownloadingLibrary.value = false
						libraryDownloadProgress.value = 0f
					}
				}
		}

		scope.launch {
			sessionManager.sessionState
				.filterIsInstance<SessionState.Authenticated>()
				.collect {
					if (downloadDao.getDownloadsByStatus(DownloadStatus.DOWNLOADING).isNotEmpty()) {
						enqueueWorker()
					}
				}
		}
	}

	suspend fun getDownloadedFilePath(songId: String): String? {
		return downloadDao.getDownloadById(songId)?.filePath
	}

	fun getCoverArtFile(coverArtId: String): File? {
		return storageManager.getCoverArtFile(coverArtId)
	}

	fun downloadSong(songId: String) {
		scope.launch {
			enqueueSongs(listOf(songId))
		}
	}

	suspend fun downloadCollection(collection: List<String>) {
		enqueueSongs(collection)
	}

	fun downloadEntireLibrary() {
		if (isDownloadingLibrary.value) return

		isDownloadingLibrary.value = true
		libraryDownloadProgress.value = 0f

		scope.launch {
			try {
				val ids = songDao.getAllSongIds()

				if (ids.isEmpty()) {
					isDownloadingLibrary.value = false
					libraryDownloadProgress.value = 1f
					return@launch
				}

				if (enqueueSongs(ids) == 0) {
					isDownloadingLibrary.value = false
					libraryDownloadProgress.value = 1f
				}
			} catch (e: Exception) {
				Logger.e("DownloadManager", "Failed to queue library download", e)
				isDownloadingLibrary.value = false
				libraryDownloadProgress.value = 0f
			}
		}
	}

	private suspend fun enqueueSongs(songIds: List<String>): Int {
		if (songIds.isEmpty()) return 0

		val existing = downloadDao.getActiveOrCompletedDownloadIds(songIds).toSet()

		val newIds = songIds
			.distinct()
			.filter { it !in existing }

		if (newIds.isEmpty()) return 0

		downloadDao.insertDownloads(
			newIds.map { DownloadEntity(it, DownloadStatus.DOWNLOADING, 0f) }
		)

		enqueueWorker()
		return newIds.size
	}

	private fun enqueueWorker() {
		val request = OneTimeWorkRequestBuilder<DownloadWorker>()
			.setConstraints(
				Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.build()
			)
			.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
			.addTag(WorkNames.DOWNLOAD_TAG)
			.build()

		workManager.enqueueUniqueWork(
			WorkNames.DOWNLOAD_QUEUE,
			ExistingWorkPolicy.APPEND_OR_REPLACE,
			request
		)
	}

	fun cancelAllActiveDownloads() {
		isDownloadingLibrary.value = false
		libraryDownloadProgress.value = 0f

		scope.launch {
			cancelActiveDownloadsInternal()
		}
	}

	private suspend fun cancelActiveDownloadsInternal() {
		workManager.cancelUniqueWork(WorkNames.DOWNLOAD_QUEUE).await()
		downloadDao.deleteDownloadsByStatus(DownloadStatus.DOWNLOADING)
		storageManager.clearTempDownloads()
		notificationManager.cancelNotification(NotificationIds.DOWNLOAD_LIBRARY)
	}

	fun cancelDownload(songId: String) {
		scope.launch {
			val existing = downloadDao.getDownloadById(songId) ?: return@launch

			when (existing.status) {
				DownloadStatus.DOWNLOADING -> downloadDao.deleteDownload(songId)

				DownloadStatus.FAILED -> {
					downloadDao.deleteDownload(songId)
					storageManager.deleteSongFiles(songId)
				}

				else -> Unit
			}
		}
	}

	fun cancelCollectionDownload(songIds: Collection<String>) {
		songIds.forEach { songId ->
			cancelDownload(songId)
		}
	}

	fun deleteDownload(songId: String) {
		scope.launch {
			val existing = downloadDao.getDownloadById(songId)
			val coverArtId = songDao.getSongById(songId)?.coverArtId
			downloadDao.deleteDownload(songId)

			val filePath = existing?.filePath
			if (filePath != null) {
				storageManager.deleteFile(filePath)
				storageManager.deleteTempFile(filePath)
			} else {
				storageManager.deleteSongFiles(songId)
			}

			if (existing != null && coverArtId != null &&
				downloadDao.countDownloadedSongsWithCoverArt(coverArtId) == 0
			) {
				storageManager.deleteCoverArt(coverArtId)
			}
		}
	}

	fun deleteDownloadedCollection(songIds: Collection<String>) {
		songIds.forEach { songId ->
			deleteDownload(songId)
		}
	}

	fun getCollectionDownloadStatus(songIds: List<String>): Flow<DownloadStatus> {
		if (songIds.isEmpty()) return flowOf(DownloadStatus.NOT_DOWNLOADED)
		return downloadDao.getDownloadsForSongsFlow(songIds).map { collectionDownloads ->
			when {
				collectionDownloads.isEmpty() -> DownloadStatus.NOT_DOWNLOADED
				collectionDownloads.any {
					it.status == DownloadStatus.DOWNLOADING
				} -> DownloadStatus.DOWNLOADING

				collectionDownloads.any { it.status == DownloadStatus.FAILED } ->
					DownloadStatus.FAILED

				(collectionDownloads.size == songIds.size &&
					collectionDownloads.all { it.status == DownloadStatus.DOWNLOADED })
					-> DownloadStatus.DOWNLOADED

				else -> DownloadStatus.NOT_DOWNLOADED
			}
		}
	}

	fun clearAllDownloads() {
		isDownloadingLibrary.value = false
		libraryDownloadProgress.value = 0f

		scope.launch {
			cancelActiveDownloadsInternal()
			storageManager.clearDownloads()
			storageManager.clearCoverArts()
			downloadDao.clearAllDownloads()
			Logger.i("DownloadManager", "cleared all downloads")
		}
	}

	fun migrateDownloads(target: DownloadStorage) {
		scope.launch {
			val request = OneTimeWorkRequestBuilder<DownloadMigrationWorker>()
				.setInputData(workDataOf(WorkKeys.STORAGE to target.name))
				.addTag(WorkNames.DOWNLOAD_TAG)
				.build()

			workManager.enqueueUniqueWork(
				WorkNames.DOWNLOAD_MIGRATION,
				ExistingWorkPolicy.REPLACE,
				request
			)

			Logger.i("DownloadManager", "started download migration to $target")
		}
	}
}
