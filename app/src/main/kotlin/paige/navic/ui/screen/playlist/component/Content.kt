/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.component

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.core.UiState

fun LazyGridScope.playlistListScreenContent(
	state: UiState<List<DomainPlaylist>>,
	selectedPlaylist: DomainPlaylist?,
	selectedViewMode: ListViewMode,
	onUpdateSelection: (DomainPlaylist) -> Unit,
	onClearSelection: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
) {
	val data = state.data.orEmpty()
	if (data.isNotEmpty()) {
		items(data, { it.id }) { playlist ->
			if (selectedViewMode == ListViewMode.Grid) {
				PlaylistListScreenGridItem(
					modifier = Modifier.animateItem(),
					tab = "playlists",
					playlist = playlist,
					selected = playlist == selectedPlaylist,
					onSelect = { onUpdateSelection(playlist) },
					onDeselect = { onClearSelection() },
					onSetShareId = onSetShareId,
					onSetDeletionId = onSetDeletionId,
					onPlayNext = onPlayNext,
					onAddToQueue = onAddToQueue,
				)
			} else {
				PlaylistListScreenListItem(
					modifier = Modifier.animateItem(),
					playlist = playlist,
					selected = playlist == selectedPlaylist,
					onSelect = { onUpdateSelection(playlist) },
					onDeselect = { onClearSelection() },
					onSetShareId = onSetShareId,
					onSetDeletionId = onSetDeletionId,
					onPlayNext = onPlayNext,
					onAddToQueue = onAddToQueue,
				)
			}
		}
	} else {
		when (state) {
			is UiState.Loading -> {
				artGridPlaceholder(viewMode = selectedViewMode)
			}

			else -> {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						label = stringResource(R.string.info_no_playlists_short)
					)
				}
			}
		}
	}
}
