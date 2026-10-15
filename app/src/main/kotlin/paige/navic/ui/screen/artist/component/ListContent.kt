/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.artist.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.toImmutableList
import paige.navic.R
import paige.navic.database.dao.LetterCount
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.ui.component.common.AlphabeticalScroller
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.screen.artist.ArtistListScreenGridItem
import paige.navic.ui.util.withoutTop

@Composable
fun ArtistListScreenContent(
	artists: LazyPagingItems<DomainArtist>,
	letterCounts: List<LetterCount>,
	starred: Boolean,
	selectedSorting: DomainArtistListType,
	gridState: LazyGridState,
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	nested: Boolean,
	selectedArtist: DomainArtist?,
	selectedArtistAlbums: List<DomainAlbum>?,
	selectedViewMode: ListViewMode,
	onUpdateSelection: (DomainArtist) -> Unit,
	onClearSelection: () -> Unit,
	onSetStarred: (Boolean) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
) {
	val totalArtistCount = artists.itemCount

	val headerIndices = remember(letterCounts) {
		var gridPosition = 1
		val list = mutableListOf<Pair<String, Int>>()
		letterCounts.forEach { lc ->
			list.add(lc.letter to gridPosition)
			gridPosition += 1 + lc.count
		}
		list.toImmutableList()
	}

	val textPadding = PaddingValues(
		horizontal = if (selectedViewMode == ListViewMode.List) 16.dp else 0.dp,
		vertical = 8.dp
	)

	Box {
		ArtGrid(
			modifier = if (!nested)
				Modifier.fillMaxSize()
					.nestedScroll(scrollBehavior.nestedScrollConnection)
			else Modifier.fillMaxSize(),
			state = gridState,
			contentPadding = innerPadding.withoutTop(),
			verticalArrangement = if (totalArtistCount == 0) {
				Arrangement.Center
			} else if (selectedViewMode == ListViewMode.List) {
				Arrangement.spacedBy(0.dp)
			} else {
				Arrangement.spacedBy(12.dp)
			},
			selectedViewMode = selectedViewMode
		) {
			if (totalArtistCount != 0) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					Row(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surface)
							.padding(textPadding.withoutTop()),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							pluralStringResource(
								R.plurals.count_artists,
								totalArtistCount,
								totalArtistCount
							),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
			if (selectedSorting == DomainArtistListType.AlphabeticalByName) {
				var artistOffset = 0
				letterCounts.forEach { lc ->
					stickyHeader {
						Row(
							modifier = Modifier
								.background(MaterialTheme.colorScheme.surface)
								.padding(textPadding),
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								text = lc.letter,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
					val startArtistIndex = artistOffset
					items(lc.count) { i ->
						val artistIndex = startArtistIndex + i
						val artist = artists[artistIndex] ?: return@items

						if (selectedViewMode == ListViewMode.Grid) {
							ArtistListScreenGridItem(
								modifier = Modifier.animateItem(),
								tab = "artists",
								artist = artist,
								selected = artist == selectedArtist,
								selectedArtistAlbums = selectedArtistAlbums,
								starred = starred,
								onSelect = { onUpdateSelection(artist) },
								onDeselect = { onClearSelection() },
								onSetStarred = { onSetStarred(it) },
								onPlayNext = onPlayNext,
								onAddToQueue = onAddToQueue
							)
						} else {
							ArtistListScreenListItem(
								modifier = Modifier.animateItem(),
								artist = artist,
								selected = artist == selectedArtist,
								selectedArtistAlbums = selectedArtistAlbums,
								starred = starred,
								onSelect = { onUpdateSelection(artist) },
								onDeselect = { onClearSelection() },
								onSetStarred = { onSetStarred(it) },
								onPlayNext = onPlayNext,
								onAddToQueue = onAddToQueue
							)
						}
					}
					artistOffset += lc.count
				}
			} else {
				items(artists.itemCount, key = { index -> artists.peek(index)?.id ?: index }) { index ->
					val artist = artists[index] ?: return@items

					if (selectedViewMode == ListViewMode.Grid) {
						ArtistListScreenGridItem(
							modifier = Modifier.animateItem(),
							tab = "artists",
							artist = artist,
							selected = artist == selectedArtist,
							selectedArtistAlbums = selectedArtistAlbums,
							starred = starred,
							onSelect = { onUpdateSelection(artist) },
							onDeselect = { onClearSelection() },
							onSetStarred = { onSetStarred(it) },
							onPlayNext = onPlayNext,
							onAddToQueue = onAddToQueue
						)
					} else {
						ArtistListScreenListItem(
							modifier = Modifier.animateItem(),
							artist = artist,
							selected = artist == selectedArtist,
							selectedArtistAlbums = selectedArtistAlbums,
							starred = starred,
							onSelect = { onUpdateSelection(artist) },
							onDeselect = { onClearSelection() },
							onSetStarred = { onSetStarred(it) },
							onPlayNext = onPlayNext,
							onAddToQueue = onAddToQueue
						)
					}
				}
			}

			if (totalArtistCount == 0) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						label = stringResource(R.string.info_no_artists)
					)
				}
			}
		}
		if (selectedSorting == DomainArtistListType.AlphabeticalByName) {
			AlphabeticalScroller(
				state = gridState,
				headers = headerIndices,
				modifier = Modifier.align(Alignment.TopEnd)
			)
		}
	}
}
