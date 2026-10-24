/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.database.dao.SyncActionDao
import paige.navic.domain.manager.session.ConnectivityManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.repository.DbRepository
import paige.navic.domain.sync.SyncCoordinator

class SettingsDataStorageViewModel(
	private val syncManager: SyncManager,
	private val syncCoordinator: SyncCoordinator,
	private val dbRepository: DbRepository,
	private val syncDao: SyncActionDao,
	private val downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : ViewModel() {

	val syncState = syncManager.syncState
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = syncManager.syncState.value
		)

	val pendingActionCount: StateFlow<Int>
		field = MutableStateFlow(0)

	val downloadCount = downloadManager.downloadCount.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), 0
	)
	val downloadSize = downloadManager.downloadSize.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), 0L
	)

	val isDownloadingLibrary = downloadManager.isDownloadingLibrary
	val libraryDownloadProgress = downloadManager.libraryDownloadProgress
	val isOnline = connectivityManager.isOnline

	init {
		loadPendingActions()
	}

	private fun loadPendingActions() {
		viewModelScope.launch(Dispatchers.IO) {
			pendingActionCount.value = syncDao.getPendingActions().size
		}
	}

	fun triggerManualSync() {
		syncManager.triggerManualSync()
	}

	fun rebuildDatabase() {
		viewModelScope.launch(Dispatchers.IO) {
			syncManager.stopPeriodicSyncAndWait()
			syncCoordinator.awaitIdle()
			dbRepository.removeEverything()
			pendingActionCount.value = 0
			syncManager.triggerManualSync()
		}
	}

	fun removeAllActions() {
		viewModelScope.launch(Dispatchers.IO) {
			syncDao.clearAllActions()
			pendingActionCount.value = 0
		}
	}

	fun clearAllDownloads() {
		downloadManager.clearAllDownloads()
	}

	fun cancelLibraryDownload() {
		downloadManager.cancelAllActiveDownloads()
	}
}
