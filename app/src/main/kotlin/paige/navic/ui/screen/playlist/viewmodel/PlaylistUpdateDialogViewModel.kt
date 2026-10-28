/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.viewmodel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import paige.navic.R
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.ui.core.UiState
import paige.navic.ui.viewmodel.AsyncViewModel
import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist

data class PlaylistUpdateViewState(
	val playlists: List<ApiPlaylist> = listOf(),
	val selectedPlaylists: Set<ApiPlaylist> = setOf()
)

class PlaylistUpdateDialogViewModel(
	private val songIds: List<String>,
	private val playlistToExclude: String?,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : AsyncViewModel<PlaylistUpdateViewState>(UiState.Loading(PlaylistUpdateViewState())) {
	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	init {
		refreshResults()
	}

	fun refreshResults() = execute {
		PlaylistUpdateViewState(
			playlists = sessionManager.api.getPlaylists()
				.filter { it.id != playlistToExclude }
		)
	}

	fun togglePlaylistSelection(playlist: ApiPlaylist) = updateData {
		it.copy(
			playlists = it.playlists,
			selectedPlaylists = if (playlist in it.selectedPlaylists) {
				it.selectedPlaylists - playlist
			} else {
				it.selectedPlaylists + playlist
			}
		)
	}

	fun confirm() {
		val data = uiState.value.data ?: return
		val selected = data.selectedPlaylists
		execute {
			selected.forEach { playlist ->
				sessionManager.api.updatePlaylist(
					playlist.id,
					songIdsToAdd = songIds
				)
			}
			if (selected.size == 1) {
				snackBarManager.notify(
					R.string.notice_added_to_playlist,
					selected.first().name
				)
			} else if (selected.size > 1) {
				snackBarManager.notify(R.string.notice_added_to_multiple_playlists)
			}
			_events.send(Event.Dismiss)
			data
		}
	}

	enum class Event {
		Dismiss
	}
}
