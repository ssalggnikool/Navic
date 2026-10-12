/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.artist.viewmodel

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.toBitmask
import paige.navic.domain.model.toDomainFilters
import paige.navic.domain.repository.ArtistRepository
import paige.navic.playback.MediaPlayer
import paige.navic.ui.viewmodel.SelectableViewModel


class ArtistListViewModel(
	initialListType: DomainArtistListType? = null,
	initialFilters: Set<DomainFilter>? = null,
	private val repository: ArtistRepository,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : SelectableViewModel<DomainArtist>() {

	val listType: StateFlow<DomainArtistListType>
		field = MutableStateFlow(initialListType ?: preferenceManager.artistSortType)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(
			initialFilters ?: preferenceManager.artistFilters.toDomainFilters()
		)

	val gridState = LazyGridState()

	init {
		launch {
			sessionManager.isLoggedIn.collect { if (it) refreshArtists(false) }
		}
	}

	fun refreshArtists(fullRefresh: Boolean) = launch {
		items = repository.getArtists(
			fullRefresh,
			listType.value,
			selectedFilters.value
		)
	}

	fun selectArtist(artist: DomainArtist?) {
		selected = artist
	}

	fun clearSelection() = selectArtist(null)

	fun starArtist(isStarred: Boolean) {
		launch {
			val artist = selected ?: return@launch
			if (isStarred) repository.starArtist(artist) else repository.unstarArtist(artist)
			refreshArtists(false)
		}
	}

	fun addArtistAlbumsToQueue(player: MediaPlayer) {
		val artist = selected ?: return
		viewModelScope.launch {
			val artistAlbums =
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.addToQueue(album)
			}
		}
	}

	fun playArtistAlbumsNext(player: MediaPlayer) {
		val artist = selected ?: return
		launch {
			val artistAlbums =
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.playNext(album)
			}
		}
	}

	fun setListType(newListType: DomainArtistListType) {
		listType.value = newListType
		preferenceManager.artistSortType = newListType
		refreshArtists(false)
	}

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.artistFilters = newFilters.toBitmask()
		refreshArtists(false)
	}

}
