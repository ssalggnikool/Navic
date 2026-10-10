/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainPlaylistListType
import paige.navic.domain.model.toBitmask
import paige.navic.domain.model.toDomainFilters
import paige.navic.domain.repository.PlaylistRepository
import paige.navic.ui.viewmodel.SelectableViewModel


class PlaylistListViewModel(
	private val repository: PlaylistRepository,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : SelectableViewModel<DomainPlaylist>() {

	val selectedSorting: StateFlow<DomainPlaylistListType>
		field = MutableStateFlow(preferenceManager.playlistSortType)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(preferenceManager.playlistSortReversed)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(preferenceManager.playlistFilters.toDomainFilters())

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshPlaylists(false) }
		}
	}

	fun refreshPlaylists(fullRefresh: Boolean) = launch {
		items = repository.getPlaylists(
			fullRefresh,
			selectedSorting.value,
			selectedReversed.value,
			selectedFilters.value
		)
	}

	fun setSorting(sorting: DomainPlaylistListType) {
		selectedSorting.value = sorting
		preferenceManager.playlistSortType = sorting
		refreshPlaylists(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		preferenceManager.playlistSortReversed = reversed
		refreshPlaylists(false)
	}

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.playlistFilters = newFilters.toBitmask()
		refreshPlaylists(false)
	}

}
