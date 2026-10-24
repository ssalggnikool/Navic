/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import android.content.Context
import androidx.annotation.StringRes
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.SyncActionDao
import paige.navic.database.entity.SyncActionEntity
import paige.navic.database.entity.SyncActionType
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.util.Logger
import paige.navic.work.LibrarySyncWorker
import paige.navic.work.SyncActionWorker
import paige.navic.work.WorkKeys
import paige.navic.work.WorkNames
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Instant

data class SyncState(
	val isSyncing: Boolean = false,
	val progress: Float = 0f,
	@StringRes val message: Int = R.string.info_status_idle
)

class SyncManager(
	context: Context,
	private val syncDao: SyncActionDao,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val workManager = WorkManager.getInstance(context)

	init {
		if (sessionManager.isLoggedIn.value) {
			startPeriodicSync()
			schedulePendingActions()
		}
	}

	val syncState: StateFlow<SyncState> = combine(
		workManager.getWorkInfosForUniqueWorkFlow(WorkNames.LIBRARY_SYNC_MANUAL),
		workManager.getWorkInfosForUniqueWorkFlow(WorkNames.LIBRARY_SYNC_PERIODIC)
	) { manual, periodic ->
		val running = (manual + periodic)
			.firstOrNull { it.state == WorkInfo.State.RUNNING }

		if (running == null) {
			SyncState()
		} else {
			SyncState(
				isSyncing = true,
				progress = running.progress.getFloat(WorkKeys.PROGRESS, 0f),
				message = running.progress.getInt(
					WorkKeys.MESSAGE,
					R.string.info_status_idle
				)
			)
		}
	}.stateIn(
		scope = scope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = SyncState()
	)

	fun startPeriodicSync(syncImmediately: Boolean = true) {
		if (!sessionManager.isLoggedIn.value) return

		schedulePeriodicWork()

		if (!syncImmediately) return

		scope.launch {
			if (albumDao.getAlbumCount() == 0 || preferenceManager.lastFullSyncTime <= 0L) {
				Logger.i("SyncManager", "Syncing now because we haven't synced before")
				enqueueManualSync(
					force = false,
					policy = ExistingWorkPolicy.KEEP
				)
			}
		}
	}

	private fun schedulePeriodicWork() {
		Logger.i("SyncManager", "Scheduling periodic sync.")

		val request =
			PeriodicWorkRequestBuilder<LibrarySyncWorker>(15, TimeUnit.MINUTES)
				.setConstraints(networkConstraints())
				.setInputData(
					workDataOf(
						WorkKeys.FORCE to false,
						WorkKeys.PERIODIC to true
					)
				)
				.setBackoffCriteria(
					BackoffPolicy.EXPONENTIAL,
					30,
					TimeUnit.SECONDS
				)
				.addTag(WorkNames.SYNC_TAG)
				.build()

		workManager.enqueueUniquePeriodicWork(
			WorkNames.LIBRARY_SYNC_PERIODIC,
			ExistingPeriodicWorkPolicy.KEEP,
			request
		)
	}

	fun triggerManualSync() {
		if (!sessionManager.isLoggedIn.value) return

		schedulePeriodicWork()
		preferenceManager.lastFullSyncTime = 0
		enqueueManualSync(force = true)
	}

	private fun enqueueManualSync(
		force: Boolean,
		policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
	) {
		val request = OneTimeWorkRequestBuilder<LibrarySyncWorker>()
			.setConstraints(networkConstraints())
			.setInputData(workDataOf(WorkKeys.FORCE to force))
			.setBackoffCriteria(
				BackoffPolicy.EXPONENTIAL,
				30,
				TimeUnit.SECONDS
			)
			.addTag(WorkNames.SYNC_TAG)
			.build()

		workManager.enqueueUniqueWork(
			WorkNames.LIBRARY_SYNC_MANUAL,
			policy,
			request
		)
	}

	fun stopPeriodicSync() {
		workManager.cancelUniqueWork(WorkNames.LIBRARY_SYNC_PERIODIC)
		workManager.cancelUniqueWork(WorkNames.LIBRARY_SYNC_MANUAL)
		workManager.cancelUniqueWork(WorkNames.SYNC_ACTIONS)
	}

	suspend fun stopPeriodicSyncAndWait() {
		workManager.cancelUniqueWork(WorkNames.LIBRARY_SYNC_PERIODIC).await()
		workManager.cancelUniqueWork(WorkNames.LIBRARY_SYNC_MANUAL).await()
		workManager.cancelUniqueWork(WorkNames.SYNC_ACTIONS).await()
	}

	fun enqueueAction(
		actionType: SyncActionType,
		itemId: String,
		time: Instant = Clock.System.now()
	) {
		scope.launch {
			syncDao.enqueue(SyncActionEntity(actionType = actionType, itemId = itemId, time = time))
			scheduleActionWorker()
		}
	}

	private fun schedulePendingActions() {
		scope.launch {
			if (syncDao.getPendingActions().isNotEmpty()) {
				scheduleActionWorker()
			}
		}
	}

	private fun scheduleActionWorker() {
		val request = OneTimeWorkRequestBuilder<SyncActionWorker>()
			.setConstraints(networkConstraints())
			.setBackoffCriteria(
				BackoffPolicy.EXPONENTIAL,
				30,
				TimeUnit.SECONDS
			)
			.addTag(WorkNames.SYNC_ACTION_TAG)
			.build()

		workManager.enqueueUniqueWork(
			WorkNames.SYNC_ACTIONS,
			ExistingWorkPolicy.APPEND_OR_REPLACE,
			request
		)
	}

	private fun networkConstraints() = Constraints.Builder()
		.setRequiredNetworkType(NetworkType.CONNECTED)
		.build()
}
