/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.data.database.mapper.toEntity
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSong
import paige.navic.ui.core.UiState

class PlaylistCreateDialogViewModel(
	private val songs: List<DomainSong>,
	private val playlistDao: PlaylistDao,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	val creationState: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()

	fun create() {
		viewModelScope.launch {
			creationState.value = UiState.Loading()
			try {
				val playlist = sessionManager.api.createPlaylist(
					name = name.text.toString(),
					songIds = songs.map { it.id }
				)
				playlistDao.insertPlaylist(playlist.toEntity())
				_events.send(
					Event.Dismiss(
						playlistDao.getPlaylistById(playlist.id)!!.toDomainModel()
					)
				)
				creationState.value = UiState.Success(null)
				snackBarManager.notify(R.string.notice_created_playlist, playlist.name)
			} catch (e: Exception) {
				creationState.value = UiState.Error(e)
			}
		}
	}

	sealed class Event {
		data class Dismiss(val playlist: DomainPlaylist) : Event()
	}
}
