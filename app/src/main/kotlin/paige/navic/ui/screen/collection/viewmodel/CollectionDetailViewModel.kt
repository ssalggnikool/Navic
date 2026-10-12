/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.collection.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import paige.navic.R
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.repository.AlbumRepository
import paige.navic.domain.repository.CollectionRepository
import paige.navic.domain.repository.SongRepository
import paige.navic.ui.viewmodel.AsyncViewModel

data class CollectionViewState(
	val collection: DomainSongCollection? = null,
	val starred: Boolean = false,
	val rating: Int = 0,
	val albumInfo: DomainAlbumInfo? = null,
	val selectedSong: DomainSong? = null,
	val selectedAlbum: DomainAlbum? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionDetailViewModel(
	private val collectionId: String,
	private val repository: CollectionRepository,
	private val songRepository: SongRepository,
	private val albumRepository: AlbumRepository,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager,
	connectivityManager: ConnectivityManager
) : AsyncViewModel<CollectionViewState>() {

	init {
		execute {
			CollectionViewState(
				collection = repository.getLocalData(collectionId)
			)
		}
	}

	val listState = LazyListState()

	val isOnline = connectivityManager.isOnline

	val otherAlbums = uiState
		.map { state -> (state.data?.collection as? DomainAlbum) }
		.flatMapLatest { album ->
			album?.let { repository.getOtherAlbums(it.artistId, it.id) } ?: emptyFlow()
		}
		.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Lazily,
		initialValue = emptyList()
	)

	init {
		launch {
			sessionManager.isLoggedIn.collect { if (it) refreshCollection(false) }
		}
	}

	fun refreshCollection(fullRefresh: Boolean) = launch {
		val collection = repository.getCollection(fullRefresh, collectionId)
		val state = if (collection is DomainAlbum) {
			CollectionViewState(
				collection = collection,
				starred = albumRepository.isAlbumStarred(collection),
				rating = albumRepository.getAlbumRating(collection),
				albumInfo = repository.getAlbumInfo(collectionId).toDomainModel()
			)
		} else {
			CollectionViewState(collection = collection)
		}
		setSuccess(state)
	}

	fun selectSong(song: DomainSong) = updateData {
		it.copy(
			selectedSong = song
		)
	}

	fun selectAlbum(album: DomainAlbum) = updateData {
		it.copy(
			selectedAlbum = album
		)
	}

	fun clearSelection() = updateData {
		it.copy(selectedSong = null, selectedAlbum = null)
	}

	fun removeFromPlaylist() {
		val song = uiState.value.data?.selectedSong ?: return
		launch {
			repository.removeSongFromPlaylist(collectionId, song.id)
			snackBarManager.notify(R.string.notice_removed_from_playlist)
			refreshCollection(true)
		}
		clearSelection()
	}

	fun starSelectedSong() = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.starSong(selection)
		refreshCollection(false)
	}

	fun unstarSelectedSong() = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.unstarSong(selection)
		refreshCollection(false)
	}

	fun rateSelectedSong(rating: Int) = launch {
		val selection = uiState.value.data?.selectedSong ?: return@launch
		songRepository.rateSong(selection, rating)
	}

	fun rateAlbum(newRating: Int) = launch {
		(uiState.value.data?.collection as? DomainAlbum)?.let { album ->
			albumRepository.rateAlbum(album, newRating)
		}
	}

	fun starAlbum(starred: Boolean) {
		launch {
			val collection = data?.collection
			if (collection !is DomainAlbum) return@launch
			if (starred) {
				albumRepository.starAlbum(collection)
			} else {
				albumRepository.unstarAlbum(collection)
			}
			refreshCollection(false)
		}
	}

	fun rateSelectedAlbum(rating: Int) {
		launch {
			data?.selectedAlbum?.let { albumRepository.rateAlbum(it, rating) }
		}
	}

	fun starSelectedAlbum(starred: Boolean) {
		launch {
			val album = data?.selectedAlbum ?: return@launch
			if (starred) {
				albumRepository.starAlbum(album)
			} else {
				albumRepository.unstarAlbum(album)
			}
		}
	}

}
