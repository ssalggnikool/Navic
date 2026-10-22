/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CancellationException
import paige.navic.R
import paige.navic.database.dao.DownloadDao
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.domain.manager.NotificationIds
import paige.navic.domain.manager.NotificationManager
import paige.navic.domain.manager.StorageManager
import paige.navic.domain.model.settings.DownloadStorage
import paige.navic.domain.repository.SongRepository
import paige.navic.util.Logger
import java.io.File

class DownloadMigrationWorker(
	context: Context,
	params: WorkerParameters,
	private val storageManager: StorageManager,
	private val downloadDao: DownloadDao,
	private val songRepository: SongRepository,
	private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		val storage = inputData.getString(WorkKeys.STORAGE)
			?.let { runCatching { DownloadStorage.valueOf(it) }.getOrNull() }
			?: return Result.failure()

		val targetDir = storageManager.getDownloadDirFor(storage)
		if (targetDir == null) {
			Logger.e(
				"DownloadMigrationWorker",
				"cannot move downloads: $storage is not available"
			)
			return Result.failure()
		}

		val downloads = downloadDao.getDownloadsByStatus(DownloadStatus.DOWNLOADED)
			.filter { it.filePath != null }

		return try {
			setForeground(createForegroundInfo(0, downloads.size))

			var completed = 0

			downloads.forEach { download ->
				val sourcePath = download.filePath ?: return@forEach
				val song = songRepository.getSongById(download.songId) ?: return@forEach

				val targetPath = storageManager.getDownloadPath(song.id, song.albumId)
				if (sourcePath != targetPath) {
					if (!File(sourcePath).exists()) {
						downloadDao.deleteDownload(download.songId)
					} else if (storageManager.moveFile(sourcePath, targetPath)) {
						downloadDao.insertDownload(
							DownloadEntity(
								songId = download.songId,
								status = DownloadStatus.DOWNLOADED,
								progress = 1f,
								filePath = targetPath
							)
						)
					} else {
						Logger.e(
							"DownloadMigrationWorker",
							"failed to move ${download.songId} to $targetPath"
						)
					}
				}

				completed++
				updateForeground(completed, downloads.size)
			}

			notificationManager.cancelNotification(NotificationIds.DOWNLOAD_MIGRATION)
			Result.success()
		} catch (e: CancellationException) {
			notificationManager.cancelNotification(NotificationIds.DOWNLOAD_MIGRATION)
			throw e
		} catch (e: Exception) {
			Logger.e("DownloadMigrationWorker", "download migration failed", e)
			notificationManager.cancelNotification(NotificationIds.DOWNLOAD_MIGRATION)
			Result.failure()
		}
	}

	private suspend fun updateForeground(completed: Int, total: Int) {
		setProgress(
			workDataOf(
				WorkKeys.COMPLETED to completed,
				WorkKeys.TOTAL to total,
				WorkKeys.PROGRESS to if (total <= 0) 0f else completed.toFloat() / total
			)
		)
		setForeground(createForegroundInfo(completed, total))
	}

	private fun createForegroundInfo(completed: Int, total: Int): ForegroundInfo {
		val progress = if (total <= 0) 0f else completed.toFloat() / total.toFloat()
		val message = if (total <= 0) {
			applicationContext.getString(R.string.info_status_moving_downloads)
		} else {
			"$completed / $total"
		}

		val title = applicationContext.getString(R.string.info_status_moving_downloads)
		val notification = notificationManager.createProgressNotification(
			id = NotificationIds.DOWNLOAD_MIGRATION,
			title = "$title (${(progress * 100).toInt()}%)",
			message = message,
			progress = progress,
			indeterminate = total <= 0
		)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ForegroundInfo(
				NotificationIds.DOWNLOAD_MIGRATION,
				notification,
				ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
			)
		} else {
			ForegroundInfo(NotificationIds.DOWNLOAD_MIGRATION, notification)
		}
	}

	override suspend fun getForegroundInfo(): ForegroundInfo {
		val total = downloadDao.countDownloadsByStatus(DownloadStatus.DOWNLOADED)
		return createForegroundInfo(0, total)
	}
}
