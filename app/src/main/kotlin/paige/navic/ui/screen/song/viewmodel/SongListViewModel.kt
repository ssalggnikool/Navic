/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.song.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.toBitmask
import paige.navic.domain.model.toDomainFilters
import paige.navic.domain.repository.SongRepository
import paige.navic.ui.viewmodel.SelectableViewModel


class SongListViewModel(
	initialListType: DomainSongListType? = null,
	initialFilters: Set<DomainFilter>? = null,
	private val repository: SongRepository,
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager,
	connectivityManager: ConnectivityManager
) : SelectableViewModel<DomainSong>() {

	val allDownloads = downloadManager.allDownloads
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = persistentListOf()
		)

	val selectedSorting = MutableStateFlow(initialListType ?: preferenceManager.songSortType)

	val selectedReversed = MutableStateFlow(preferenceManager.songSortReversed)

	val selectedFilters = MutableStateFlow(
		initialFilters ?: preferenceManager.songFilters.toDomainFilters()
	)

	val isOnline = connectivityManager.isOnline

	init {
		launch {
			sessionManager.isLoggedIn.collect { if (it) refreshSongs(false) }
		}
	}

	fun selectSong(song: DomainSong?) {
		selected = song
	}

	fun clearSelection() = clear()

	fun refreshSongs(fullRefresh: Boolean) = launch {
		items = repository.getSongs(
			fullRefresh,
			selectedSorting.value,
			selectedReversed.value,
			selectedFilters.value
		)
	}

	fun starSong(isStarred: Boolean) {
		launch {
			val selection = selected ?: return@launch
			if (isStarred) {
				repository.starSong(selection)
			} else {
				repository.unstarSong(selection)
			}
			refreshSongs(false)
		}
	}

	fun rateSelectedSong(rating: Int) {
		launch {
			val selection = selected ?: return@launch
			repository.rateSong(selection, rating)
		}
	}

	fun setSorting(sorting: DomainSongListType) {
		selectedSorting.value = sorting
		preferenceManager.songSortType = sorting
		refreshSongs(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		preferenceManager.songSortReversed = reversed
		refreshSongs(false)
	}

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.songFilters = newFilters.toBitmask()
		refreshSongs(false)
	}

	fun clearError() {
		// ?
	}

	fun downloadSong(song: DomainSong) {
		downloadManager.downloadSong(song)
	}

	fun cancelDownload(songId: String) {
		downloadManager.cancelDownload(songId)
	}

	fun deleteDownload(songId: String) {
		downloadManager.deleteDownload(songId)
	}
}
