/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import paige.navic.R
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.CreatePlaylistContent
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.entity.PlaylistSongCrossRef
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.domain.model.DomainPlaylist
import paige.navic.ui.core.UiState
import paige.navic.ui.viewmodel.AsyncViewModel
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration

class PlaylistCreateDialogViewModel(
	private val songIds: List<String>,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val syncManager: SyncManager,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : AsyncViewModel<Nothing?>(UiState.Success(null)) {
	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()

	fun create() {
		launch {
			setLoading()
			val playlistName = name.text.toString().trim()
			val localId = "local_${UUID.randomUUID()}"
			val now = Clock.System.now()

			val songs = songDao.getSongsByIds(songIds)
			val totalDuration = songs.map { it.duration }.fold(Duration.ZERO) { acc, dur -> acc + dur }

			val entity = PlaylistEntity(
				playlistId = localId,
				name = playlistName,
				comment = null,
				owner = sessionManager.username,
				coverArtId = songs.firstOrNull()?.coverArtId,
				songCount = songIds.size,
				duration = totalDuration,
				public = false,
				readOnly = false,
				createdAt = now,
				modifiedAt = now,
				validUntil = null,
				allowedUsers = emptyList()
			)

			playlistDao.insertPlaylist(entity)
			val crossRefs = songIds.mapIndexed { index, songId ->
				PlaylistSongCrossRef(playlistId = localId, songId = songId, position = index)
			}
			playlistDao.insertPlaylistSongCrossRefs(crossRefs)

			val content = Json.encodeToString(CreatePlaylistContent(name = playlistName, songIds = songIds))
			syncManager.enqueueAction(
				actionType = SyncActionType.CREATE_PLAYLIST,
				itemId = localId,
				content = content
			)

			snackBarManager.notify(R.string.notice_created_playlist, playlistName)
			setSuccess(null)

			val createdPlaylist = playlistDao.getPlaylistById(localId)?.toDomainModel()
				?: entity.toDomainModel()
			_events.send(Event.Dismiss(createdPlaylist))
		}
	}

	sealed class Event {
		data class Dismiss(val playlist: DomainPlaylist) : Event()
	}
}
