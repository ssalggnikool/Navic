/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.download.viewmodel

import paige.navic.database.entity.DownloadEntity
import paige.navic.domain.manager.DownloadManager
import paige.navic.ui.viewmodel.AsyncViewModel
import paige.navic.ui.viewmodel.SelectableList

class DownloadListViewModel(
	private val downloadManager: DownloadManager
) : AsyncViewModel<SelectableList<DownloadEntity>>() {
	var items
		get() = data?.items
		set(value) {
			execute {
				SelectableList(
					items = value,
					selected = selected
				)
			}
		}

	var selected
		get() = data?.selected
		set(value) {
			execute {
				SelectableList(
					items = items,
					selected = value
				)
			}
		}

	init {
		launch {
			downloadManager.allDownloads.collect {
				items = it
			}
		}
	}

	fun deleteDownload(download: DownloadEntity) {
		downloadManager.cancelDownload(download.songId)
	}
}
