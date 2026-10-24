/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.album.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.database.dao.LetterCount
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.toBitmask
import paige.navic.domain.model.toDomainFilters
import paige.navic.domain.repository.AlbumRepository
import paige.navic.ui.viewmodel.SelectableViewModel

class AlbumListViewModel(
	initialListType: DomainAlbumListType? = null,
	initialFilters: Set<DomainFilter>? = null,
	private val repository: AlbumRepository,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : SelectableViewModel<DomainAlbum>() {
	val listType: StateFlow<DomainAlbumListType>
		field = MutableStateFlow(initialListType ?: preferenceManager.albumSortType)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(preferenceManager.albumSortReversed)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(
			initialFilters ?: preferenceManager.albumFilters.toDomainFilters()
		)

	val letterCounts: StateFlow<List<LetterCount>>
		field = MutableStateFlow(emptyList())

	val gridState = LazyGridState()

	private var letterCountsJob: Job? = null

	init {
		launch {
			sessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) = launch {
		items = repository.getAlbums(
			fullRefresh, listType.value,
			selectedReversed.value,
			selectedFilters.value
		)
		letterCountsJob?.cancel()
		letterCountsJob = viewModelScope.launch {
			repository.getAlbumLetterCounts(selectedReversed.value, selectedFilters.value).collect {
				letterCounts.value = it
			}
		}
	}

	fun starAlbum(isStarred: Boolean) {
		launch {
			val selection = selected ?: return@launch
			if (isStarred) repository.starAlbum(selection.id) else repository.unstarAlbum(selection.id)
			refreshAlbums(false)
		}
	}

	fun setRating(newRating: Int) {
		launch {
			val selection = selected ?: return@launch
			repository.rateAlbum(selection.id, newRating)
			refreshAlbums(false)
		}
	}

	fun setListType(newListType: DomainAlbumListType) {
		listType.value = newListType
		preferenceManager.albumSortType = newListType
		refreshAlbums(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		preferenceManager.albumSortReversed = reversed
		refreshAlbums(false)
	}

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.albumFilters = newFilters.toBitmask()
		refreshAlbums(false)
	}
}
