/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.genre.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainGenre
import paige.navic.domain.repository.GenreRepository
import paige.navic.ui.viewmodel.AsyncViewModel

class GenreListViewModel(
	private val repository: GenreRepository,
	private val sessionManager: SessionManager
) : AsyncViewModel<ImmutableList<DomainGenre>>() {

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshGenres(false) }
		}
	}

	fun refreshGenres(fullRefresh: Boolean) {
		launch {
			setLoading()
			repository.getGenresFlow(fullRefresh).collect {
				setSuccess(it)
			}
		}
	}

}
