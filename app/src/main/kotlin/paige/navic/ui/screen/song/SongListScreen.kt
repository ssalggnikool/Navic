/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.R.string.info_no_songs
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.dialog.QueueDuplicateDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.PullToRefreshBox
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.component.layout.RootTopBar
import paige.navic.ui.core.UiState
import paige.navic.ui.navigation.PersistentViewModelStoreOwner
import paige.navic.ui.screen.share.dialog.ShareDialog
import paige.navic.ui.screen.song.component.SongListScreenItem
import paige.navic.ui.screen.song.component.SongListScreenSortButton
import paige.navic.ui.screen.song.viewmodel.SongListViewModel
import paige.navic.ui.util.withoutTop
import paige.navic.ui.viewmodel.RootViewModel
import kotlin.time.Duration

@Composable
fun SongListScreen(
	nested: Boolean,
	listType: DomainSongListType? = null
) {
	val viewModel = koinViewModel<SongListViewModel>(
		key = listType.toString(),
		parameters = { parametersOf(listType) },
		viewModelStoreOwner = if (nested) {
			LocalViewModelStoreOwner.current!!
		} else {
			koinInject<PersistentViewModelStoreOwner>()
		}
	)
	val preferenceManager = koinInject<PreferenceManager>()
	val player = koinInject<MediaPlayer>()
	val state by viewModel.uiState.collectAsStateWithLifecycle()
	val downloadManager = koinInject<DownloadManager>()

	val selectedSorting by viewModel.selectedSorting.collectAsStateWithLifecycle()
	val selectedReversed by viewModel.selectedReversed.collectAsStateWithLifecycle()
	val selectedFilters by viewModel.selectedFilters.collectAsStateWithLifecycle()
	val allDownloads by downloadManager.allDownloads.collectAsStateWithLifecycle(
		initialValue = emptyList()
	)

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	val actions: @Composable RowScope.() -> Unit = {
		SongListScreenSortButton(
			nested = nested,
			selectedSorting = selectedSorting,
			onSetSorting = { viewModel.setSorting(it) },
			selectedReversed = selectedReversed,
			onSetReversed = { viewModel.setReversed(it) },
			selectedFilters = selectedFilters,
			onToggleFilter = { viewModel.toggleFilter(it) }
		)
	}

	val rootViewModel = koinViewModel<RootViewModel>()
	val listState = rememberLazyListState()
	LaunchedEffect(Unit) {
		rootViewModel.events.collect { event ->
			if (event is RootViewModel.Event.ScrollToTop) {
				listState.animateScrollToItem(0)
			}
		}
	}

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					title = { Text(stringResource(R.string.title_songs)) },
					scrollBehavior = scrollBehavior,
					actions = actions
				)
			} else {
				NestedTopBar(
					title = { Text(stringResource(R.string.title_songs)) },
					actions = actions
				)
			}
		},
		bottomBar = {
			val sizeClass = LocalSizeClass.current
			val scrollManager = LocalBottomBarScrollManager.current
			val preferVisible =
				preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens
			if (!nested
				|| (sizeClass.widthSizeClass < WindowWidthSizeClass.Medium && preferVisible)) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = state !is UiState.Loading,
			onRefresh = { viewModel.refreshSongs(true) },
			key = state
		) {
			LazyColumn(
				modifier = if (!nested)
					Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
				else Modifier.fillMaxSize(),
				contentPadding = innerPadding.withoutTop(),
				verticalArrangement = if ((state as? UiState.Success)?.data?.items?.isEmpty() == true)
					Arrangement.Center
				else Arrangement.spacedBy(12.dp),
				state = listState
			) {
				val songs = state.data?.items.orEmpty()
				if (songs.isNotEmpty()) {
					items(songs.size) { index ->
						val song = songs[index]

						val download = allDownloads.find { it.songId == song.id }
						SongListScreenItem(
							modifier = Modifier.animateItem(),
							song = song,
							selected = song == state.data?.selected,
							starred = song.starredAt != null,
							rating = song.userRating ?: 0,
							onSelect = {
								viewModel.selected = song
							},
							onDeselect = {
								viewModel.clear()
							},
							onSetStarred = {
								viewModel.starSong(it)
							},
							onSetShareId = { newShareId: String ->
								shareId = newShareId
							},
							onPlayNext = {
								if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
									songToQueue = song
								} else {
									player.playNextSingle(song)
								}
							},
							onAddToQueue = {
								if (player.uiState.value.queue.any { it.id == song.id } && !preferenceManager.shushQueueDuplicateDialog) {
									songToQueue = song
								} else {
									player.addToQueueSingle(song)
								}
							},
							onClick = {
								player.playNow(song)
							},
							onSetRating = { it: Int -> viewModel.rateSelectedSong(it) },
							download = download,
							onDownload = {
								downloadManager.downloadSong(song)
							},
							onCancelDownload = {
								downloadManager.cancelDownload(song.id)
							},
							onDeleteDownload = {
								downloadManager.deleteDownload(song.id)
							}
						)
					}
				} else {
					when (state) {
						is UiState.Loading -> {
							// TODO
						}

						else -> {
							item {
								ContentUnavailable(
									label = stringResource(info_no_songs)
								)
							}
						}
					}
				}
			}
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
