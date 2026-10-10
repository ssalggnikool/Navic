/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainRadio
import paige.navic.domain.repository.RadioRepository
import paige.navic.ui.viewmodel.AsyncViewModel

class RadioListViewModel(
	private val repository: RadioRepository,
	private val sessionManager: SessionManager
) : AsyncViewModel<List<DomainRadio>>() {
	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshRadios(false) }
		}
	}

	fun refreshRadios(fullRefresh: Boolean) {
		launch {
			repository.getRadios(fullRefresh)
		}
	}

}
