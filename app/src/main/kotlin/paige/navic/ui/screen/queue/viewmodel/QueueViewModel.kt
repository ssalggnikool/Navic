/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.queue.viewmodel

import androidx.lifecycle.ViewModel
import paige.navic.domain.manager.session.connectivity.ReachabilityFlow
import paige.navic.domain.manager.session.DownloadManager

class QueueViewModel(
	reachability: ReachabilityFlow,
	downloadManager: DownloadManager
) : ViewModel() {
	val isReachable = reachability.isReachable
	val downloadedSongs = downloadManager.downloadedSongs
}
