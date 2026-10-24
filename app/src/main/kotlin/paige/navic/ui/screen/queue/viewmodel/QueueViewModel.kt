/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.queue.viewmodel

import androidx.lifecycle.ViewModel
import paige.navic.domain.manager.session.ConnectivityManager
import paige.navic.domain.manager.session.DownloadManager

class QueueViewModel(
	connectivityManager: ConnectivityManager,
	downloadManager: DownloadManager
) : ViewModel() {
	val isOnline = connectivityManager.isOnline
	val downloadedSongs = downloadManager.downloadedSongs
}
