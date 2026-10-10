/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.genre

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.dialog.QueueDuplicateDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.PullToRefreshBox
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.component.snackbar.ErrorSnackBar
import paige.navic.ui.core.UiState
import paige.navic.ui.screen.album.viewmodel.AlbumListViewModel
import paige.navic.ui.screen.genre.component.GenreDetailScreenContent
import paige.navic.ui.screen.share.dialog.ShareDialog
import paige.navic.ui.screen.song.viewmodel.SongListViewModel
import kotlin.time.Duration

@Composable
fun GenreDetailScreen(
	genreName: String
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val player = koinInject<MediaPlayer>()
	val downloadManager = koinInject<DownloadManager>()

	val songsViewModel = koinViewModel<SongListViewModel>(
		key = "genre_detail_songs_$genreName",
		parameters = { parametersOf(DomainSongListType.ByGenre(genreName)) }
	)
	val songsState by songsViewModel.uiState.collectAsStateWithLifecycle()

	val albumsViewModel = koinViewModel<AlbumListViewModel>(
		key = "genre_detail_albums_$genreName",
		parameters = { parametersOf(DomainAlbumListType.ByGenre(genreName)) }
	)
	val albumsState by albumsViewModel.uiState.collectAsStateWithLifecycle()

	val allDownloads by downloadManager.allDownloads.collectAsStateWithLifecycle(
		initialValue = emptyList()
	)
	val isOnline by songsViewModel.isOnline.collectAsStateWithLifecycle()

	var shareId by rememberSaveable { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }

	Scaffold(
		topBar = { NestedTopBar({ Text(genreName) }) },
		bottomBar = {
			val sizeClass = LocalSizeClass.current
			val scrollManager = LocalBottomBarScrollManager.current
			val preferVisible =
				preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens
			if (sizeClass.widthSizeClass < WindowWidthSizeClass.Medium && preferVisible) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = albumsState !is UiState.Loading &&
				songsState !is UiState.Loading,
			onRefresh = {
				albumsViewModel.refreshAlbums(true)
				songsViewModel.refreshSongs(true)
			},
			key = listOf(albumsState, songsState)
		) {
			GenreDetailScreenContent(
				genreName = genreName,
				innerPadding = innerPadding,
				onSetShareId = { shareId = it },
				isOnline = isOnline,

				songs = songsState.data?.items.orEmpty(),
				selectedSong = songsState.data?.selected,
				selectedSongRating = songsState.data?.selected?.userRating ?: 0,
				allDownloads = allDownloads,
				onSelectSong = {
					songsViewModel.selected = it
				},
				onClearSongSelection = { songsViewModel.clear() },
				onAddSongStar = { songsViewModel.starSong(true) },
				onRemoveSongStar = { songsViewModel.starSong(false) },
				onPlaySongNext = { song ->
					if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
						songToQueue = song
					} else {
						player.playNextSingle(song)
					}
				},
				onAddSongToQueue = { song ->
					if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
						songToQueue = song
					} else {
						player.addToQueueSingle(song)
					}
				},
				onPlaySong = { index ->
					player.playNow(songsState.data?.items.orEmpty(), index)
				},
				onSetSongRating = { songsViewModel.rateSelectedSong(it) },
				onDownloadSong = { downloadManager.downloadSong(it) },
				onCancelDownloadSong = { song ->
					downloadManager.cancelDownload(song.id)
				},
				onDeleteDownloadSong = { song ->
					downloadManager.deleteDownload(song.id)
				},

				albumsState = albumsState.data?.items.orEmpty(),
				selectedAlbum = albumsState.data?.selected,
				selectedAlbumIsStarred = albumsState.data?.selected?.starredAt != null,
				selectedAlbumRating = albumsState.data?.selected?.userRating ?: 0,
				onSelectAlbum = {
					albumsViewModel.selected = it
				},
				onClearAlbumSelection = {
					albumsViewModel.selected = null
				},
				onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },
				onPlayAlbumNext = { albumsState.data?.selected.let { player.playNext(it as DomainSongCollection) } },
				onAddAlbumToQueue = { albumsState.data?.selected.let { player.addToQueue(it as DomainSongCollection) } },
				onRateSelectedAlbum = { albumsViewModel.setRating(it) },
			)
		}
	}

	val flattenedErrors = listOf(
		(albumsState as? UiState.Error)?.error,
		(songsState as? UiState.Error)?.error
	).mapNotNull { it?.stackTraceToString() }.takeIf { it.isNotEmpty() }?.joinToString("\n\n")

	ErrorSnackBar(
		error = flattenedErrors?.let { Error(it) },
		onClearError = {
			albumsViewModel.clearError()
			songsViewModel.clearError()
		}
	)

	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	if (songToQueue != null) {
		QueueDuplicateDialog(
			onDismissRequest = { songToQueue = null },
			onConfirm = {
				songToQueue?.let { player.addToQueueSingle(it) }
			}
		)
	}
}
