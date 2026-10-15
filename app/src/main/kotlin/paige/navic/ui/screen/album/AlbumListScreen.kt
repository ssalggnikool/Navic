/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.media3.common.util.UnstableApi
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.R.string.info_no_albums
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.common.AlphabeticalScroller
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.PullToRefreshBox
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.component.layout.RootTopBar
import paige.navic.ui.component.snackbar.ErrorSnackBar
import paige.navic.ui.core.UiState
import paige.navic.ui.navigation.PersistentViewModelStoreOwner
import paige.navic.ui.screen.album.component.AlbumListScreenGridItem
import paige.navic.ui.screen.album.component.AlbumListScreenListItem
import paige.navic.ui.screen.album.component.AlbumListScreenSortButton
import paige.navic.ui.screen.album.viewmodel.AlbumListViewModel
import paige.navic.ui.screen.share.dialog.ShareDialog
import paige.navic.ui.util.withoutTop
import paige.navic.ui.viewmodel.RootViewModel
import kotlin.time.Duration

@UnstableApi
@Composable
fun AlbumListScreen(
	nested: Boolean = false,
	listType: DomainAlbumListType? = null
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val selectedViewMode = preferenceManager.albumListViewMode

	val viewModel = koinViewModel<AlbumListViewModel>(
		key = listType.toString(),
		parameters = { parametersOf(listType) },
		viewModelStoreOwner = if (nested) {
			LocalViewModelStoreOwner.current!!
		} else {
			koinInject<PersistentViewModelStoreOwner>()
		}
	)
	val player = koinInject<MediaPlayer>()
	val selectedSorting by viewModel.listType.collectAsStateWithLifecycle()
	val selectedReversed by viewModel.selectedReversed.collectAsStateWithLifecycle()
	val selectedFilters by viewModel.selectedFilters.collectAsStateWithLifecycle()
	val state by viewModel.uiState.collectAsStateWithLifecycle()
	val lazyPagingItems = state.data?.items?.collectAsLazyPagingItems()

	val selectedAlbum = state.data?.selected
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	val actions: @Composable RowScope.() -> Unit = {
		AlbumListScreenSortButton(
			nested = nested,
			selectedSorting = selectedSorting,
			onSetSorting = { viewModel.setListType(it) },
			selectedReversed = selectedReversed,
			onSetReversed = { viewModel.setReversed(it) },
			selectedViewMode = selectedViewMode,
			onSetViewMode = { preferenceManager.albumListViewMode = it },
			selectedFilters = selectedFilters,
			onToggleFilter = { viewModel.toggleFilter(it) }
		)
	}

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
			if (!nested) {
				RootTopBar(
					{ Text(stringResource(R.string.title_albums)) },
					scrollBehavior,
					actions
				)
			} else {
				NestedTopBar({ Text(stringResource(R.string.title_albums)) }, actions)
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
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = state !is UiState.Loading,
			onRefresh = { viewModel.refreshAlbums(true) },
			key = state
		) {
			val snapshotList = lazyPagingItems?.itemSnapshotList?.items.orEmpty()
			val grouped = remember(state.data?.items) {
				snapshotList.groupBy { it.name?.firstOrNull()?.uppercaseChar() ?: '#' }
					.toList()
					.sortedBy { it.first }
			}

			val headerIndices = remember(grouped) {
				var currentIndex = 0
				grouped.map { (letter, albums) ->
					val pos = currentIndex
					currentIndex += albums.size + 1
					letter.toString() to pos
				}
			}
			Box {
				ArtGrid(
					modifier = if (!nested)
						Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
					else Modifier,
					state = viewModel.gridState,
					contentPadding = innerPadding.withoutTop(),
					verticalArrangement = if (lazyPagingItems == null || lazyPagingItems.itemCount == 0) {
						Arrangement.Center
					} else if (selectedViewMode == ListViewMode.List) {
						Arrangement.spacedBy(0.dp)
					} else {
						Arrangement.spacedBy(12.dp)
					},
					selectedViewMode = selectedViewMode
				) {
					lazyPagingItems?.let { items ->
						if (items.itemCount > 0) {
							if (selectedSorting == DomainAlbumListType.AlphabeticalByName) {
								val grouped = snapshotList.groupBy {
									it.name?.firstOrNull()?.uppercaseChar() ?: '#'
								}
									.toList()
									.sortedBy { it.first }

								val textPadding = PaddingValues(
									horizontal = if (selectedViewMode == ListViewMode.List) 16.dp else 0.dp,
									vertical = 8.dp
								)

								grouped.forEach { (letter, albums) ->
									stickyHeader {
										Row(
											modifier = Modifier
												.background(MaterialTheme.colorScheme.surface)
												.padding(textPadding),
											verticalAlignment = Alignment.CenterVertically
										) {
											Text(
												text = letter.toString(),
												color = MaterialTheme.colorScheme.onSurfaceVariant
											)
										}
									}
									items(albums, { it.id }) { album ->
										if (selectedViewMode == ListViewMode.Grid) {
											AlbumListScreenGridItem(
												modifier = Modifier.animateItem(),
												tab = "albums",
												album = album,
												selected = album == selectedAlbum,
												starred = selectedAlbum?.starredAt != null,
												onSelect = {
													viewModel.selected = album
												},
												onDeselect = {
													viewModel.selected = null
												},
												onSetStarred = {
													viewModel.starAlbum(it)
												},
												onSetShareId = {
													shareId = it
												},
												onPlayNext = {
													if (selectedAlbum != null) player.playNext(
														selectedAlbum as DomainSongCollection
													)
												},
												onAddToQueue = {
													if (selectedAlbum != null) {
														player.addToQueue(
															selectedAlbum as DomainSongCollection
														)
													}
												},
												rating = selectedAlbum?.userRating ?: 0,
												onSetRating = { it: Int -> viewModel.setRating(it) }
											)
										} else {
											AlbumListScreenListItem(
												modifier = Modifier.animateItem(),
												album = album,
												selected = album == selectedAlbum,
												starred = selectedAlbum?.starredAt != null,
												onSelect = {
													viewModel.selected = album
												},
												onDeselect = {
													viewModel.selected = null
												},
												onSetStarred = {
													viewModel.starAlbum(it)
												},
												onSetShareId = { newShareId: String ->
													shareId = newShareId
												},
												onPlayNext = {
													if (selectedAlbum != null) player.playNext(
														selectedAlbum as DomainSongCollection
													)
												},
												onAddToQueue = {
													if (selectedAlbum != null) {
														player.addToQueue(
															selectedAlbum as DomainSongCollection
														)
													}
												},
												rating = selectedAlbum?.userRating ?: 0,
												onSetRating = { it: Int -> viewModel.setRating(it) }
											)
										}
									}
								}
							} else {
								items(
									items.itemCount,
									{ index -> items[index]?.id ?: 0 }) { index ->
									val album = items[index] ?: return@items

									if (selectedViewMode == ListViewMode.Grid) {
										AlbumListScreenGridItem(
											modifier = Modifier.animateItem(),
											tab = "albums",
											album = album,
											selected = album == selectedAlbum,
											starred = selectedAlbum?.starredAt != null,
											onSelect = {
												viewModel.selected = album
											},
											onDeselect = {
												viewModel.selected = null
											},
											onSetStarred = {
												viewModel.starAlbum(it)
											},
											onSetShareId = {
												shareId = it
											},
											onPlayNext = {
												if (selectedAlbum != null) player.playNext(
													selectedAlbum as DomainSongCollection
												)
											},
											onAddToQueue = {
												if (selectedAlbum != null) {
													player.addToQueue(
														selectedAlbum as DomainSongCollection
													)
												}
											},
											rating = selectedAlbum?.userRating ?: 0,
											onSetRating = { it: Int -> viewModel.setRating(it) }
										)
									} else {
										AlbumListScreenListItem(
											modifier = Modifier.animateItem(),
											album = album,
											selected = album == selectedAlbum,
											starred = selectedAlbum?.starredAt != null,
											onSelect = {
												viewModel.selected = album
											},
											onDeselect = {
												viewModel.selected = null
											},
											onSetStarred = {
												viewModel.starAlbum(it)
											},
											onSetShareId = { newShareId: String ->
												shareId = newShareId
											},
											onPlayNext = {
												if (selectedAlbum != null) player.playNext(
													selectedAlbum as DomainSongCollection
												)
											},
											onAddToQueue = {
												if (selectedAlbum != null) {
													player.addToQueue(
														selectedAlbum as DomainSongCollection
													)
												}
											},
											rating = selectedAlbum?.userRating ?: 0,
											onSetRating = { it: Int -> viewModel.setRating(it) }
										)
									}
								}
							}

						} else {
							item(span = { GridItemSpan(maxLineSpan) }) {
								ContentUnavailable(label = stringResource(info_no_albums))
							}
						}
					}
				}
				if (selectedSorting == DomainAlbumListType.AlphabeticalByName) {
					AlphabeticalScroller(
						state = viewModel.gridState,
						headers = headerIndices.toImmutableList(),
						modifier = Modifier.align(Alignment.TopEnd)
					)
				}
			}
		}
	}

	ErrorSnackBar(
		error = (state as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)

	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}
