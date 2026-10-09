/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.starred

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.playback.MediaPlayerViewModel
import paige.navic.ui.component.dialog.QueueDuplicateDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.PullToRefreshBox
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.core.UiState
import paige.navic.ui.navigation.PersistentViewModelStoreOwner
import paige.navic.ui.screen.album.viewmodel.AlbumListViewModel
import paige.navic.ui.screen.artist.viewmodel.ArtistListViewModel
import paige.navic.ui.screen.share.dialog.ShareDialog
import paige.navic.ui.screen.song.viewmodel.SongListViewModel
import paige.navic.ui.screen.starred.component.StarredScreenContent
import kotlin.time.Duration

@Composable
fun StarredScreen() {
	val persistentViewModelStoreOwner = koinInject<PersistentViewModelStoreOwner>()
	val preferenceManager = koinInject<PreferenceManager>()

	val songsViewModel = koinViewModel<SongListViewModel>(
		key = "starredSongs",
		parameters = {
			parametersOf(
				DomainSongListType.FrequentlyPlayed,
				setOf(DomainFilter.Starred)
			)
		},
		viewModelStoreOwner = persistentViewModelStoreOwner
	)
	val songsState by songsViewModel.uiState.collectAsStateWithLifecycle()
	val allDownloads by songsViewModel.allDownloads.collectAsStateWithLifecycle()

	val albumsViewModel = koinViewModel<AlbumListViewModel>(
		key = "starredAlbums",
		parameters = {
			parametersOf(
				DomainAlbumListType.AlphabeticalByArtist,
				setOf(DomainFilter.Starred)
			)
		},
		viewModelStoreOwner = persistentViewModelStoreOwner
	)
	val albumsState by albumsViewModel.uiState.collectAsStateWithLifecycle()

	val artistsViewModel = koinViewModel<ArtistListViewModel>(
		key = "starredArtists",
		parameters = {
			parametersOf(
				DomainArtistListType.AlphabeticalByName,
				setOf(DomainFilter.Starred)
			)
		},
		viewModelStoreOwner = persistentViewModelStoreOwner
	)
	val artistsState by artistsViewModel.uiState.collectAsStateWithLifecycle()

	var shareId by rememberSaveable { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val player = koinInject<MediaPlayerViewModel>()

	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }

	val isOnline by songsViewModel.isOnline.collectAsStateWithLifecycle()

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.title_starred)) }) },
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
			finished = !(albumsState is UiState.Loading ||
				artistsState is UiState.Loading ||
				songsState is UiState.Loading),
			onRefresh = {
				albumsViewModel.refreshAlbums(true)
				artistsViewModel.refreshArtists(true)
				songsViewModel.refreshSongs(true)
			},
			key = listOf(albumsState, artistsState, songsState)
		) {
			val selectedAlbum = albumsState.data?.selected
			val selectedArtist = artistsState.data?.selected

			StarredScreenContent(
				innerPadding = innerPadding,
				onSetShareId = { shareId = it },
				isOnline = isOnline,

				songs = songsState.data?.items.orEmpty(),
				selectedSong = songsState.data?.selected,
				allDownloads = allDownloads,
				onPlaySong = { index ->
					songsState.data?.items.let {
						player.playNow(it.orEmpty(), index)
					}
				},
				onSelectSong = {
					songsViewModel.selected = it
				},
				onClearSongSelection = { songsViewModel.clear() },
				selectedSongIsStarred = songsState.data?.selected?.starredAt != null,
				onAddSongStar = { songsViewModel.starSong(true) },
				onRemoveSongStar = { songsViewModel.starSong(false) },
				onDownloadSong = { songsViewModel.downloadSong(it) },
				onCancelDownloadSong = { song ->
					songsViewModel.cancelDownload(song.id)
				},
				onDeleteDownloadSong = { song ->
					songsViewModel.deleteDownload(song.id)
				},
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
				selectedSongRating = songsState.data?.selected?.userRating ?: 0,
				onSetSongRating = { songsViewModel.rateSelectedSong(it) },

				albums = albumsState.data?.items ?: emptyList(),
				selectedAlbum = selectedAlbum,
				selectedAlbumIsStarred = selectedAlbum?.starredAt != null,
				selectedAlbumRating = albumsState.data?.selected?.userRating ?: 0,
				onSelectAlbum = {
					albumsViewModel.selected = it
				},
				onClearAlbumSelection = {
					albumsViewModel.selected = null
				},
				onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },
				onPlayAlbumNext = { if (selectedAlbum != null) player.playNext(selectedAlbum as DomainSongCollection) },
				onAddAlbumToQueue = { if (selectedAlbum != null) player.addToQueue(selectedAlbum as DomainSongCollection) },
				onRateSelectedAlbum = { albumsViewModel.setRating(it) },

				artists = artistsState.data?.items ?: emptyList(),
				selectedArtist = artistsState.data?.selected,
				// TODO: artist albums
				selectedArtistAlbums = emptyList(),
				selectedArtistIsStarred = false,
				onSelectArtist = { artistsViewModel.selectArtist(it) },
				onClearArtistSelection = { artistsViewModel.clearSelection() },
				onStarSelectedArtist = { artistsViewModel.starArtist(it) },
				onPlayArtistNext = {
					if (selectedArtist != null) artistsViewModel.playArtistAlbumsNext(
						player
					)
				},
				onAddArtistToQueue = {
					if (selectedArtist != null) artistsViewModel.addArtistAlbumsToQueue(
						player
					)
				},
			)
		}
	}

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
