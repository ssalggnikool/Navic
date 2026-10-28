/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SessionState
import paige.navic.domain.repository.DbRepository
import paige.navic.util.Logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class SyncCoordinator(
	private val repository: DbRepository,
	private val actionProcessor: SyncActionProcessor,
	private val preferenceManager: PreferenceManager,
	private val sessionManager: SessionManager
) {
	private val mutex = Mutex()
	private val fullSyncThreshold = 1.hours

	val isLoggedIn: Boolean
		get() = sessionManager.sessionState.value is SessionState.Authenticated

	fun isFullSyncDue(force: Boolean = false): Boolean {
		return force || preferenceManager.lastFullSyncTime <= 0L ||
			Clock.System.now() - Instant.fromEpochMilliseconds(
			preferenceManager.lastFullSyncTime
		) > fullSyncThreshold
	}

	suspend fun run(
		force: Boolean = false,
		onProgress: suspend (Float, Int) -> Unit = { _, _ -> }
	): Result<Unit> = mutex.withLock {
		if (!isLoggedIn) return@withLock Result.success(Unit)

		val actionResult = actionProcessor.processPending()

		if (!isFullSyncDue(force)) {
			return@withLock actionResult
		}

		Logger.i("SyncCoordinator", "Starting full library pull...")

		val result = repository.syncEverything(onProgress)
		result.onSuccess {
			preferenceManager.lastFullSyncTime = Clock.System.now().toEpochMilliseconds()
			Logger.i("SyncCoordinator", "Full library sync complete.")
		}.onFailure {
			if (it is CancellationException) throw it
			Logger.e("SyncCoordinator", "couldn't sync library", it)
		}

		result
	}

	suspend fun processActions(): Result<Unit> = mutex.withLock {
		if (!isLoggedIn) return@withLock Result.success(Unit)
		actionProcessor.processPending()
	}

	suspend fun awaitIdle() {
		mutex.withLock { }
	}
}
