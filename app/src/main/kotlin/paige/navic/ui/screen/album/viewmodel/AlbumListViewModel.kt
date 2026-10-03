/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.album.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.toBitmask
import paige.navic.domain.model.toDomainFilters
import paige.navic.domain.repository.AlbumRepository
import paige.navic.ui.core.UiState

class AlbumListViewModel(
	initialListType: DomainAlbumListType? = null,
	initialFilters: Set<DomainFilter>? = null,
	private val repository: AlbumRepository,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val albumsState: StateFlow<UiState<ImmutableList<DomainAlbum>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainAlbum>>>(UiState.Loading())

	val selectedAlbum: StateFlow<DomainAlbum?>
		field = MutableStateFlow(null)

	val starred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val rating: StateFlow<Int>
		field = MutableStateFlow(0)

	val listType: StateFlow<DomainAlbumListType>
		field = MutableStateFlow(initialListType ?: preferenceManager.albumSortType)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(preferenceManager.albumSortReversed)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(
			initialFilters ?: preferenceManager.albumFilters.toDomainFilters()
		)

	val gridState = LazyGridState()

	private val pageSize = 30
	private var currentPage = 0
	private var canLoadMore = true
	private var isLoadingPage = false

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) {
		viewModelScope.launch {
			currentPage = 0
			canLoadMore = true
			isLoadingPage = true

			val currentData = if (fullRefresh) persistentListOf() else (albumsState.value.data ?: persistentListOf())
			albumsState.value = UiState.Loading(data = currentData)

			try {
				if (fullRefresh) {
					repository.syncLibrary()
				}
				val initialPage = repository.getAlbumsPage(
					page = 0,
					pageSize = pageSize,
					listType = listType.value,
					reversed = selectedReversed.value,
					filters = selectedFilters.value
				)
				canLoadMore = initialPage.size >= pageSize
				albumsState.value = UiState.Success(data = initialPage)
			} catch (error: Exception) {
				albumsState.value = UiState.Error(error = error, data = albumsState.value.data ?: persistentListOf())
			} finally {
				isLoadingPage = false
			}
		}
	}

	fun loadNextPage() {
		if (!canLoadMore || isLoadingPage) return
		viewModelScope.launch {
			isLoadingPage = true
			try {
				val nextPage = currentPage + 1
				val newItems = repository.getAlbumsPage(
					page = nextPage,
					pageSize = pageSize,
					listType = listType.value,
					reversed = selectedReversed.value,
					filters = selectedFilters.value
				)
				if (newItems.isEmpty()) {
					canLoadMore = false
				} else {
					currentPage = nextPage
					canLoadMore = newItems.size >= pageSize
					val currentList = albumsState.value.data ?: persistentListOf()
					albumsState.value = UiState.Success(data = (currentList + newItems).toImmutableList())
				}
			} catch (error: Exception) {
				albumsState.value = UiState.Error(error = error, data = albumsState.value.data ?: persistentListOf())
			} finally {
				isLoadingPage = false
			}
		}
	}

	fun selectAlbum(album: DomainAlbum) {
		viewModelScope.launch {
			selectedAlbum.value = album
			starred.value = repository.isAlbumStarred(album)
			rating.value = repository.getAlbumRating(album)
		}
	}

	fun clearSelection() {
		selectedAlbum.value = null
	}

	fun starAlbum(isStarred: Boolean) {
		viewModelScope.launch {
			val selection = selectedAlbum.value ?: return@launch
			runCatching {
				if (isStarred) {
					repository.starAlbum(selection)
				} else {
					repository.unstarAlbum(selection)
				}
				starred.value = isStarred
			}
		}
	}

	fun setRating(newRating: Int) {
		viewModelScope.launch {
			val selection = selectedAlbum.value ?: return@launch
			runCatching {
				rating.value = newRating
				repository.rateAlbum(selection, newRating)
			}
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

	fun clearError() {
		albumsState.value = UiState.Success(albumsState.value.data ?: persistentListOf())
	}
}
