/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.genre.component

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import paige.navic.R
import paige.navic.database.entity.DownloadEntity
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType
import paige.navic.ui.component.common.SongRow
import paige.navic.ui.component.layout.header
import paige.navic.ui.component.layout.horizontalSection
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.album.component.AlbumListScreenGridItem
import paige.navic.ui.util.withoutTop

@Composable
fun GenreDetailScreenContent(
	genreName: String,
	innerPadding: PaddingValues,
	onSetShareId: (String) -> Unit,
	isOnline: Boolean,

	songs: LazyPagingItems<DomainSong>,
	selectedSong: DomainSong?,
	selectedSongRating: Int,
	allDownloads: List<DownloadEntity>,
	onSelectSong: (DomainSong) -> Unit,
	onClearSongSelection: () -> Unit,
	onAddSongStar: () -> Unit,
	onRemoveSongStar: () -> Unit,
	onPlaySongNext: (DomainSong) -> Unit,
	onAddSongToQueue: (DomainSong) -> Unit,
	onPlaySong: (Int) -> Unit,
	onSetSongRating: (Int) -> Unit,
	onDownloadSong: (DomainSong) -> Unit,
	onCancelDownloadSong: (DomainSong) -> Unit,
	onDeleteDownloadSong: (DomainSong) -> Unit,

	// albums
	albumsState: LazyPagingItems<DomainAlbum>,
	selectedAlbum: DomainAlbum?,
	selectedAlbumIsStarred: Boolean,
	selectedAlbumRating: Int,
	onSelectAlbum: (DomainAlbum) -> Unit,
	onClearAlbumSelection: () -> Unit,
	onStarSelectedAlbum: (Boolean) -> Unit,
	onRateSelectedAlbum: (Int) -> Unit,
	onPlayAlbumNext: () -> Unit,
	onAddAlbumToQueue: () -> Unit,
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		contentPadding = innerPadding.withoutTop() + PaddingValues(top = 8.dp),
		verticalArrangement = Arrangement.spacedBy(5.dp),
		horizontalArrangement = Arrangement.spacedBy(5.dp),
	) {
		header(
			title = R.string.title_songs,
			destination = Screen.SongList(true, DomainSongListType.ByGenre(genreName)),
			active = true
		)
		item(span = { GridItemSpan(maxLineSpan) }) {
			val rowCount = remember(songs.itemCount) {
				songs.itemCount.coerceIn(1, 3)
			}
			val gridHeight = remember(rowCount) {
				when (rowCount) {
					1 -> 82.dp
					2 -> 164.dp
					else -> 246.dp
				}
			}
			val gridState = rememberLazyGridState()
			LazyHorizontalGrid(
				rows = GridCells.Fixed(rowCount),
				state = gridState,
				flingBehavior = rememberSnapFlingBehavior(lazyGridState = gridState),
				modifier = Modifier
					.fillMaxWidth()
					.height(gridHeight)
			) {
				items(
					songs.itemCount
				) { index ->
					val song = songs[index] ?: return@items

					val download = allDownloads.find { it.songId == song.id }
					SongRow(
						song = song,
						selected = selectedSong == song,
						onClick = { onPlaySong(index) },
						onLongClick = { onSelectSong(song) },
						onDismissRequest = { onClearSongSelection() },
						starredState = song.starredAt != null,
						onAddStar = onAddSongStar,
						onRemoveStar = onRemoveSongStar,
						download = download,
						onDownload = { onDownloadSong(song) },
						onCancelDownload = { onCancelDownloadSong(song) },
						onDeleteDownload = { onDeleteDownloadSong(song) },
						onPlayNext = { onPlaySongNext(song) },
						onAddToQueue = { onAddSongToQueue(song) },
						onShare = { onSetShareId(song.id) },
						isOnline = isOnline,
						rating = selectedSongRating,
						onSetRating = { onSetSongRating(it) }
					)
				}
			}
		}

		horizontalSection(
			title = R.string.title_albums,
			destination = Screen.AlbumList(true, DomainAlbumListType.ByGenre(genreName)),
			state = albumsState,
			seeAll = true
		) { album ->
			AlbumListScreenGridItem(
				modifier = Modifier
					.animateItem()
					.width(150.dp),
				tab = "genre",
				album = album,
				selected = album == selectedAlbum,
				starred = selectedAlbumIsStarred,
				onSelect = { onSelectAlbum(album) },
				onDeselect = onClearAlbumSelection,
				onSetStarred = onStarSelectedAlbum,
				onSetShareId = onSetShareId,
				onPlayNext = onPlayAlbumNext,
				onAddToQueue = onAddAlbumToQueue,
				rating = selectedAlbumRating,
				onSetRating = onRateSelectedAlbum
			)
		}
	}
}
