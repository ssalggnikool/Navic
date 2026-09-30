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
import paige.navic.shared.MediaPlayerViewModel
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
	val songsState by songsViewModel.songsState.collectAsStateWithLifecycle()
	val selectedSong by songsViewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by songsViewModel.starred.collectAsStateWithLifecycle()
	val selectedSongRating by songsViewModel.selectedSongRating.collectAsStateWithLifecycle()
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
	val albumsState by albumsViewModel.albumsState.collectAsStateWithLifecycle()
	val selectedAlbum by albumsViewModel.selectedAlbum.collectAsStateWithLifecycle()
	val selectedAlbumIsStarred by albumsViewModel.starred.collectAsStateWithLifecycle()
	val selectedAlbumRating by albumsViewModel.rating.collectAsStateWithLifecycle()

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
	val artistsState by artistsViewModel.artistsState.collectAsStateWithLifecycle()
	val selectedArtist by artistsViewModel.selectedArtist.collectAsStateWithLifecycle()
	val selectedArtistAlbums by artistsViewModel.selectedArtistAlbums.collectAsStateWithLifecycle()
	val selectedArtistIsStarred by artistsViewModel.starred.collectAsStateWithLifecycle()

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
		val isAnythingLoading = albumsState is UiState.Loading ||
			artistsState is UiState.Loading ||
			songsState is UiState.Loading
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = !isAnythingLoading,
			onRefresh = {
				albumsViewModel.refreshAlbums(true)
				artistsViewModel.refreshArtists(true)
				songsViewModel.refreshSongs(true)
			},
			key = listOf(albumsState, artistsState, songsState)
		) {
			StarredScreenContent(
				innerPadding = innerPadding,
				onSetShareId = { shareId = it },
				isOnline = isOnline,

				songsState = songsState,
				selectedSong = selectedSong,
				allDownloads = allDownloads,
				onPlaySong = { index ->
					player.playNow(songsState.data.orEmpty(), index)
				},
				onSelectSong = {
					songsViewModel.selectSong(it)
				},
				onClearSongSelection = { songsViewModel.clearSelection() },
				selectedSongIsStarred = selectedSongIsStarred,
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
				selectedSongRating = selectedSongRating,
				onSetSongRating = { songsViewModel.rateSelectedSong(it) },

				albumsState = albumsState,
				selectedAlbum = selectedAlbum,
				selectedAlbumIsStarred = selectedAlbumIsStarred,
				selectedAlbumRating = selectedAlbumRating,
				onSelectAlbum = { albumsViewModel.selectAlbum(it) },
				onClearAlbumSelection = { albumsViewModel.clearSelection() },
				onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },
				onPlayAlbumNext = { if (selectedAlbum != null) player.playNext(selectedAlbum as DomainSongCollection) },
				onAddAlbumToQueue = { if (selectedAlbum != null) player.addToQueue(selectedAlbum as DomainSongCollection) },
				onRateSelectedAlbum = { albumsViewModel.setRating(it) },

				artistsState = artistsState,
				selectedArtist = selectedArtist,
				selectedArtistAlbums = selectedArtistAlbums,
				selectedArtistIsStarred = selectedArtistIsStarred,
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
