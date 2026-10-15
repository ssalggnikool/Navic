/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainRadio
import paige.navic.domain.repository.RadioRepository
import paige.navic.ui.viewmodel.AsyncViewModel

class RadioListViewModel(
	private val repository: RadioRepository,
	private val sessionManager: SessionManager
) : AsyncViewModel<Flow<PagingData<DomainRadio>>>() {
	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshRadios(false) }
		}
	}

	fun selectRadio(radio: DomainRadio?) {
		viewModelScope.launch {
			selectedRadio.value = radio
		}
	}

	fun refreshRadios(fullRefresh: Boolean) {
		execute {
			repository.getRadios(fullRefresh)
		}
	}
}
