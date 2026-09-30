/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.album.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.data.database.entity.DownloadStatus
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.ui.component.layout.ArtGridItem
import paige.navic.ui.component.sheet.CollectionSheet
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.playlist.dialog.PlaylistUpdateDialog

@Composable
fun AlbumListScreenGridItem(
	modifier: Modifier = Modifier,
	tab: String,
	album: DomainAlbum,
	selected: Boolean,
	starred: Boolean,
	rating: Int,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit,
	onSetShareId: (String) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	onSetRating: (Int) -> Unit
) {
	val backStack = LocalNavStack.current
	val snackBarManager = koinInject<SnackBarManager>()
	val scope = rememberCoroutineScope()

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	val downloadManager = koinInject<DownloadManager>()
	val downloadStatus by downloadManager
		.getCollectionDownloadStatus(album.songs.map { it.id })
		.collectAsState(initial = DownloadStatus.NOT_DOWNLOADED)

	Box(modifier) {
		ArtGridItem(
			onClick = dropUnlessResumed {
				scope.launch {
					backStack.add(Screen.CollectionDetail(album.id, tab))
				}
			},
			onLongClick = onSelect,
			coverArtId = album.coverArtId,
			title = album.name ?: "[unknown album]",
			subtitle = album.artistName,
			id = album.id,
			tab = tab
		)
		if (selected) {
			CollectionSheet(
				onDismissRequest = onDeselect,
				collection = album,
				onShare = { onSetShareId(album.id) },
				onPlayNext = onPlayNext,
				onAddToQueue = onAddToQueue,
				downloadStatus = downloadStatus,
				onDownloadAll = {
					scope.launch {
						downloadManager.downloadCollection(album)
						snackBarManager.notify(R.string.notice_download_started)
					}
				},
				onCancelDownloadAll = {
					scope.launch {
						album.songs.forEach { downloadManager.cancelDownload(it.id) }
					}
				},
				onDeleteDownloadAll = {
					scope.launch {
						downloadManager.deleteDownloadedCollection(album)
						snackBarManager.notify(R.string.notice_deleted_download)
					}
				},
				starred = starred,
				onSetStarred = onSetStarred,
				onAddAllToPlaylist = { playlistDialogShown = true },
				onViewArtist = dropUnlessResumed {
					backStack.add(Screen.ArtistDetail(album.artistId))
				},
				rating = rating,
				onSetRating = onSetRating
			)
		}

		if (playlistDialogShown) {
			PlaylistUpdateDialog(
				songs = album.songs.toPersistentList(),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}
}
