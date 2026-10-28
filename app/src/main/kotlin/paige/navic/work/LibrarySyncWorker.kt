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
import paige.navic.domain.manager.runtime.NotificationIds
import paige.navic.domain.manager.runtime.NotificationManager
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.util.Logger

class LibrarySyncWorker(
	context: Context,
	params: WorkerParameters,
	private val syncCoordinator: SyncCoordinator,
	private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		if (!syncCoordinator.isLoggedIn) return Result.success()

		val force = inputData.getBoolean(WorkKeys.FORCE, false)
		val isPeriodic = inputData.getBoolean(WorkKeys.PERIODIC, false)
		val showForeground = syncCoordinator.isFullSyncDue(force)

		if (showForeground) {
			setForeground(createForegroundInfo(0f, R.string.info_syncing))
		}

		return try {
			val result = syncCoordinator.run(force) { progress, message ->
				setProgress(
					workDataOf(
						WorkKeys.PROGRESS to progress,
						WorkKeys.MESSAGE to message
					)
				)

				setForeground(createForegroundInfo(progress, message))
			}

			if (result.isSuccess) {
				Result.success()
			} else {
				Logger.e("LibrarySyncWorker", "library sync failed", result.exceptionOrNull())
				retryOrFinish(isPeriodic)
			}
		} catch (e: CancellationException) {
			throw e
		} catch (e: Exception) {
			Logger.e("LibrarySyncWorker", "library sync failed", e)
			retryOrFinish(isPeriodic)
		}
	}

	private fun retryOrFinish(isPeriodic: Boolean): Result {
		return when {
			runAttemptCount < 3 -> Result.retry()
			isPeriodic -> Result.success()
			else -> Result.failure()
		}
	}

	override suspend fun getForegroundInfo(): ForegroundInfo {
		return createForegroundInfo(0f, R.string.info_syncing)
	}

	private fun createForegroundInfo(progress: Float, message: Int): ForegroundInfo {
		val title = applicationContext.getString(R.string.title_sync_control)
		val notification = notificationManager.createProgressNotification(
			id = NotificationIds.SYNC_LIBRARY,
			title = "$title (${(progress * 100).toInt()}%)",
			message = applicationContext.getString(message),
			progress = progress,
			indeterminate = progress <= 0f
		)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ForegroundInfo(
				NotificationIds.SYNC_LIBRARY,
				notification,
				ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
			)
		} else {
			ForegroundInfo(NotificationIds.SYNC_LIBRARY, notification)
		}
	}
}
