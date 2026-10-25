/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.search

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.dialog.DeleteDownloadTarget
import paige.navic.ui.component.dialog.rememberDownloadDeletionHandler
import paige.navic.ui.component.dialog.QueueDuplicateDialog
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.component.layout.horizontalSection
import paige.navic.ui.core.UiState
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Close
import paige.navic.ui.icons.outlined.History
import paige.navic.ui.navigation.PersistentViewModelStoreOwner
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.album.component.AlbumListScreenGridItem
import paige.navic.ui.screen.album.viewmodel.AlbumListViewModel
import paige.navic.ui.screen.artist.ArtistListScreenGridItem
import paige.navic.ui.screen.artist.viewmodel.ArtistListViewModel
import paige.navic.ui.screen.search.component.SearchScreenChips
import paige.navic.ui.screen.search.component.SearchScreenTopBar
import paige.navic.ui.screen.search.viewmodel.SearchViewModel
import paige.navic.ui.screen.song.component.SongListScreenItem
import paige.navic.ui.viewmodel.RootViewModel

enum class SearchCategory(@StringRes val res: Int) {
	ALL(R.string.title_all),
	SONGS(R.string.title_songs),
	ALBUMS(R.string.title_albums),
	ARTISTS(R.string.title_artists)
}

// TODO: clean this up, holy shit
@Composable
fun SearchScreen(
	nested: Boolean
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val downloadManager = koinInject<DownloadManager>()
	val allDownloads by downloadManager.allDownloads.collectAsStateWithLifecycle(initialValue = emptyList())
	val downloadsMap = remember(allDownloads) { allDownloads.associateBy { it.songId } }

	val viewModel = koinViewModel<SearchViewModel>(
		viewModelStoreOwner = if (nested) {
			LocalViewModelStoreOwner.current!!
		} else {
			koinInject<PersistentViewModelStoreOwner>()
		}
	)
	val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by viewModel.selectedSongIsStarred.collectAsStateWithLifecycle()
	val selectedSongRating by viewModel.selectedSongRating.collectAsStateWithLifecycle()

	val artistListViewModel = koinViewModel<ArtistListViewModel> {
		parametersOf(DomainArtistListType.AlphabeticalByName)
	}
	val artistListSelection by artistListViewModel.uiState.collectAsState()

	val albumListViewModel = koinViewModel<AlbumListViewModel> {
		parametersOf(DomainAlbumListType.AlphabeticalByName)
	}
	val albumListSelection by albumListViewModel.uiState.collectAsState()

	val query = viewModel.searchQuery
	val state by viewModel.searchState.collectAsState()
	val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())

	val player = koinInject<MediaPlayer>()

	var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }
	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }
	var shareId by remember { mutableStateOf<String?>(null) }
	val downloadDeletionHandler = rememberDownloadDeletionHandler()

	val rootViewModel = koinViewModel<RootViewModel>()
	LaunchedEffect(Unit) {
		rootViewModel.events.collect { event ->
			if (event is RootViewModel.Event.ScrollToTop) {
				viewModel.gridState.animateScrollToItem(0)
			}
		}
	}

	Scaffold(
		topBar = {
			Column(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface)
					.padding(
						TopAppBarDefaults.windowInsets.asPaddingValues()
					)
			) {
				SearchScreenTopBar(
					query = query,
					nested = nested,
					onSearch = { submittedQuery ->
						viewModel.addToSearchHistory(submittedQuery)
					}
				)
				SearchScreenChips(
					selectedCategory = selectedCategory,
					onCategorySelect = { selectedCategory = it }
				)
			}
		},
		bottomBar = {
			val sizeClass = LocalSizeClass.current
			val scrollManager = LocalBottomBarScrollManager.current
			val preferVisible =
				preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens
			if (!nested
				|| (sizeClass.widthSizeClass < WindowWidthSizeClass.Medium && preferVisible)
			) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		AnimatedContent(
			state,
			modifier = Modifier.fillMaxSize()
		) { uiState ->
			when (uiState) {
				is UiState.Loading -> ArtGrid(
					contentPadding = contentPadding,
					selectedViewMode = ListViewMode.List
				) {
					artGridPlaceholder(viewMode = ListViewMode.List)
				}

				is UiState.Error -> ErrorBox(uiState, padding = contentPadding)
				is UiState.Success -> {
					val results = uiState.data
					val showAll = selectedCategory == SearchCategory.ALL
					val albums =
						if (showAll || selectedCategory == SearchCategory.ALBUMS) results.filterIsInstance<DomainAlbum>() else emptyList()
					val artists =
						if (showAll || selectedCategory == SearchCategory.ARTISTS) results.filterIsInstance<DomainArtist>() else emptyList()
					val songs =
						if (showAll || selectedCategory == SearchCategory.SONGS) results.filterIsInstance<DomainSong>() else emptyList()

					if (query.text.isNotBlank() && albums.isEmpty() && artists.isEmpty() && songs.isEmpty()) {
						ContentUnavailable(
							label = stringResource(R.string.info_no_search_results)
						)
					}

					LazyVerticalGrid(
						modifier = Modifier.fillMaxSize(),
						columns = GridCells.Fixed(2),
						contentPadding = contentPadding,
						state = viewModel.gridState,
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						if (query.text.isNotBlank()) {
							if (songs.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(
										stringResource(R.string.title_songs),
										style = MaterialTheme.typography.headlineSmall,
										modifier = Modifier.padding(
											horizontal = 16.dp,
											vertical = 8.dp
										)
									)
								}
								val displaySongs = if (showAll) songs.take(10) else songs
								items(
									displaySongs.size,
									span = { GridItemSpan(maxLineSpan) }
								) { index ->
									val song = displaySongs[index]
									val download = downloadsMap[song.id]

									SongListScreenItem(
										modifier = Modifier.animateItem(),
										song = song,
										selected = selectedSong == song,
										starred = if (selectedSong == song) selectedSongIsStarred else song.starredAt != null,
										rating = if (selectedSong == song) selectedSongRating else (song.userRating
											?: 0),
										onSelect = { viewModel.selectSong(song) },
										onDeselect = { viewModel.clearSelectedSong() },
										onSetStarred = { viewModel.starSelectedSong(it) },
										onSetShareId = { shareId = it },
										onPlayNext = {
											if (player.checkDuplicate(song.id, preferenceManager)) {
												songToQueue = song
											} else {
												player.playNextSingle(song)
											}
										},
										onAddToQueue = {
											if (player.checkDuplicate(song.id, preferenceManager)) {
												songToQueue = song
											} else {
												player.addToQueueSingle(song)
											}
										},
										onClick = { player.playNow(song) },
										onSetRating = { viewModel.rateSelectedSong(it) },
										download = download,
										onDownload = { downloadManager.downloadSong(song.id) },
										onCancelDownload = { downloadManager.cancelDownload(song.id) },
										onDeleteDownload = {
											downloadDeletionHandler.requestDelete(DeleteDownloadTarget.Single(song.id, song.title))
										}
									)
								}
							}

							horizontalSection(
								title = R.string.title_albums,
								destination = Screen.AlbumList(true),
								state = albums,
								seeAll = false
							) { album ->
								AlbumListScreenGridItem(
									modifier = Modifier
										.animateItem(fadeInSpec = null)
										.width(150.dp),
									tab = "search",
									album = album,
									selected = album == albumListSelection.data?.selected,
									starred = albumListSelection.data?.selected?.starredAt != null,
									onSelect = {
										albumListViewModel.selected = album
									},
									onDeselect = {
										albumListViewModel.selected = null
									},
									onSetStarred = { albumListViewModel.starAlbum(it) },
									onSetShareId = { },
									onPlayNext = { player.playNext(album as DomainSongCollection) },
									onAddToQueue = { player.addToQueue(album as DomainSongCollection) },
									rating = albumListSelection.data?.selected?.userRating ?: 0,
									onSetRating = { albumListViewModel.setRating(it) }
								)
							}

							horizontalSection(
								title = R.string.title_artists,
								destination = Screen.ArtistList(true),
								state = artists,
								seeAll = false
							) { artist ->
								ArtistListScreenGridItem(
									modifier = Modifier
										.animateItem(fadeInSpec = null)
										.width(150.dp),
									tab = "search",
									artist = artist,
									selected = artist == artistListSelection.data?.selected,
									selectedArtistAlbums = emptyList(),
									starred = artistListSelection.data?.selected?.starredAt != null,
									onSelect = { artistListViewModel.selectArtist(artist) },
									onDeselect = { artistListViewModel.clearSelection() },
									onSetStarred = { artistListViewModel.starArtist(it) },
									onPlayNext = { artistListViewModel.playArtistAlbumsNext(player) },
									onAddToQueue = {
										artistListViewModel.addArtistAlbumsToQueue(
											player
										)
									}
								)
							}
						} else {
							if (searchHistory.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(
										text = stringResource(R.string.action_search_history),
										style = MaterialTheme.typography.titleMedium,
										color = MaterialTheme.colorScheme.primary,
										modifier = Modifier.padding(
											horizontal = 20.dp,
											vertical = 12.dp
										)
									)
								}
								items(
									searchHistory.size,
									span = { GridItemSpan(maxLineSpan) }) { index ->
									val historyItem = searchHistory[index]
									ListItem(
										modifier = Modifier.clickable {
											query.clearText()
											query.edit { insert(0, historyItem) }
										},
										content = { Text(historyItem) },
										leadingContent = {
											Icon(
												imageVector = Icons.Outlined.History,
												contentDescription = null,
												tint = MaterialTheme.colorScheme.onSurfaceVariant
											)
										},
										trailingContent = {
											IconButton(onClick = {
												viewModel.removeFromSearchHistory(historyItem)
											}) {
												Icon(
													imageVector = Icons.Outlined.Close,
													contentDescription = stringResource(R.string.action_remove_from_history),
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										}
									)
								}
							}
						}
					}
				}
			}
		}
	}

	QueueDuplicateDialog(
		song = songToQueue,
		onDismissRequest = { songToQueue = null }
	)

	ShareDialog(
		id = shareId,
		onDismissRequest = {
			shareId = null
			viewModel.clearSelectedSong()
		}
	)
}
