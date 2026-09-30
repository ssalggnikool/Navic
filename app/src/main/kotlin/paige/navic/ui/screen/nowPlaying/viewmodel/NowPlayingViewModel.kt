/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import paige.navic.domain.repository.SongRepository
import paige.navic.shared.MediaPlayerViewModel

class NowPlayingViewModel(
	private val player: MediaPlayerViewModel,
	private val songRepository: SongRepository
) : ViewModel(), KoinComponent {
	val songIsStarred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val songRating: StateFlow<Int>
		field = MutableStateFlow(0)

	init {
		viewModelScope.launch {
			player.uiState.collect { state ->
				state.currentSong?.let { song ->
					songIsStarred.value = songRepository.isSongStarred(song)
					songRating.value = songRepository.getSongRating(song)
				}
			}
		}
	}

	fun starSong(starred: Boolean) {
		viewModelScope.launch {
			runCatching {
				player.uiState.value.currentSong?.let { song ->
					songIsStarred.value = starred
					if (starred) {
						songRepository.starSong(song)
					} else {
						songRepository.unstarSong(song)
					}
				}
			}
		}
	}

	fun rateSong(rating: Int) {
		viewModelScope.launch {
			runCatching {
				player.uiState.value.currentSong?.let { song ->
					songRating.value = rating
					songRepository.rateSong(song, rating)
				}
			}
		}
	}
}
