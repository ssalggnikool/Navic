/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.collection.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import paige.navic.data.database.entity.DownloadStatus
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.dialog.QueueDuplicateDialog
import paige.navic.ui.component.sheet.SongSheet
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.playlist.dialog.PlaylistUpdateDialog

@Composable
fun CollectionDetailScreenSongRowDropdown(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	onRemoveStar: () -> Unit,
	onAddStar: () -> Unit,
	onShare: () -> Unit,
	collection: DomainSongCollection,
	song: DomainSong,
	onRemoveFromPlaylist: () -> Unit,
	starred: Boolean,
	downloadStatus: DownloadStatus?,
	onDownload: () -> Unit,
	onCancelDownload: () -> Unit,
	onDeleteDownload: () -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	rating: Int,
	onSetRating: (Int) -> Unit
) {
	val player = koinInject<MediaPlayerViewModel>()
	val preferenceManager = koinInject<PreferenceManager>()
	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	var isPlayNextPending by rememberSaveable { mutableStateOf<Boolean?>(null) }

	if (expanded) {
		SongSheet(
			onDismissRequest = onDismissRequest,
			song = song,
			collection = collection,
			starred = starred,
			onSetStarred = { starred ->
				if (starred) onAddStar() else onRemoveStar()
			},
			onShare = onShare,
			onPlayNext = {
				if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
					isPlayNextPending = true
				} else {
					onPlayNext()
				}
			},
			onAddToQueue = {
				if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
					isPlayNextPending = false
				} else {
					onAddToQueue()
				}
			},
			onTrackInfo = dropUnlessResumed {
				backStack.add(Screen.SongDetailScreen(song.id, song.coverArtId))
			},
			onViewAlbum = if (collection !is DomainAlbum && song.albumId != null) {
				dropUnlessResumed {
					backStack.add(
						Screen.CollectionDetail(
							collectionId = song.albumId,
							tab = "library"
						)
					)
				}
			} else null,
			onViewArtist = dropUnlessResumed {
				backStack.add(Screen.ArtistDetail(song.artistId))
			},
			onAddToPlaylist = {
				playlistDialogShown = true
			},
			onRemoveFromPlaylist = onRemoveFromPlaylist,
			downloadStatus = downloadStatus,
			onDownload = onDownload,
			onCancelDownload = onCancelDownload,
			onDeleteDownload = onDeleteDownload,
			rating = rating,
			onSetRating = onSetRating
		)
	}

	if (playlistDialogShown) {
		PlaylistUpdateDialog(
			songs = persistentListOf(song),
			playlistToExclude = if (collection is DomainPlaylist)
				collection.id
			else null,
			onDismissRequest = { playlistDialogShown = false }
		)
	}

	if (isPlayNextPending != null) {
		QueueDuplicateDialog(
			onDismissRequest = {
				isPlayNextPending = null
				onDismissRequest()
			},
			onConfirm = {
				if (isPlayNextPending == true) onPlayNext() else onAddToQueue()
				isPlayNextPending = null
			}
		)
	}
}
