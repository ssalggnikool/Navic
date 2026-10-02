/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainPlaylistListType
import paige.navic.domain.model.settings.ListViewMode
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Sort
import paige.navic.ui.component.layout.TopBarButton
import paige.navic.ui.component.sheet.SortSheet

@Composable
fun PlaylistListScreenSortButton(
	nested: Boolean,
	selectedSorting: DomainPlaylistListType,
	onSetSorting: (DomainPlaylistListType) -> Unit,
	selectedReversed: Boolean,
	onSetReversed: (Boolean) -> Unit,
	selectedViewMode: ListViewMode,
	onSetViewMode: (ListViewMode) -> Unit,
	selectedFilters: Set<DomainFilter>,
	onToggleFilter: (DomainFilter) -> Unit
) {
	val entries = remember { DomainPlaylistListType.entries.toImmutableList() }
	var expanded by remember { mutableStateOf(false) }
	if (!nested) {
		IconButton(onClick = {
			expanded = true
		}) {
			Icon(
				imageVector = Icons.Outlined.Sort,
				contentDescription = null
			)
		}
	} else {
		TopBarButton(onClick = { expanded = true }) {
			Icon(
				imageVector = Icons.Outlined.Sort,
				contentDescription = null
			)
		}
	}
	if (expanded) {
		SortSheet(
			entries = entries,
			selectedSorting = selectedSorting,
			selectedReversed = selectedReversed,
			label = { stringResource(it.displayName) },
			onSetSorting = onSetSorting,
			onSetReversed = onSetReversed,
			onDismissRequest = { expanded = false },
			selectedViewMode = selectedViewMode,
			onSetViewMode = onSetViewMode,
			selectedFilters = selectedFilters,
			onToggleFilter = onToggleFilter
		)
	}
}
