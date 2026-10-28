/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.util.Logger

class SyncActionWorker(
	context: Context,
	params: WorkerParameters,
	private val syncCoordinator: SyncCoordinator
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		return try {
			val result = syncCoordinator.processActions()

			if (result.isSuccess) {
				Result.success()
			} else {
				Logger.e("SyncActionWorker", "sync actions failed", result.exceptionOrNull())
				if (runAttemptCount < 5) Result.retry() else Result.failure()
			}
		} catch (e: CancellationException) {
			throw e
		} catch (e: Exception) {
			Logger.e("SyncActionWorker", "sync actions failed", e)
			if (runAttemptCount < 5) Result.retry() else Result.failure()
		}
	}
}
