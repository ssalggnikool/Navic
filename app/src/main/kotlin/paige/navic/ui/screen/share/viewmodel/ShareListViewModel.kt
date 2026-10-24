/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.share.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.model.DomainShare
import paige.navic.domain.repository.ShareRepository
import paige.navic.ui.viewmodel.AsyncViewModel
import paige.navic.ui.viewmodel.SelectableList

class ShareListViewModel(
	private val repository: ShareRepository,
	private val sessionManager: SessionManager
) : AsyncViewModel<SelectableList<DomainShare>>() {
	val gridState = LazyGridState()
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
			sessionManager.isLoggedIn.collect {
				refreshShares()
			}
		}
	}

	fun refreshShares() {
		launch {
			items = repository.getShares()
		}
	}
}
