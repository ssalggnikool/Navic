/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.database.entity.DownloadStatus
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.RootBottomBar
import paige.navic.ui.component.snackbar.ErrorSnackBar
import paige.navic.ui.core.UiState
import paige.navic.ui.screen.download.component.DownloadListHeader
import paige.navic.ui.screen.download.component.DownloadListItem
import paige.navic.ui.screen.download.dialog.DownloadSheet
import paige.navic.ui.screen.download.viewmodel.DownloadListViewModel

@Composable
fun DownloadListScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	val viewModel = koinViewModel<DownloadListViewModel>()
	val state by viewModel.uiState.collectAsStateWithLifecycle()

	val groupState = remember { mutableStateMapOf<DownloadStatus, Boolean>() }

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.title_downloads)) }) },
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap),
			contentPadding = contentPadding
		) {
			val items = state.data?.items ?: return@LazyColumn
			val groupedItems = items.groupBy { it.status }

			groupedItems.onEach { (status, items) ->
				val expanded = groupState[status] ?: false
				val count = items.count()

				stickyHeader {
					val label = when (status) {
						DownloadStatus.DOWNLOADING -> stringResource(
							R.string.title_downloads_in_progress,
							count
						)

						DownloadStatus.FAILED -> stringResource(
							R.string.title_downloads_failed,
							count
						)

						DownloadStatus.DOWNLOADED -> stringResource(
							R.string.title_downloads_complete,
							count
						)

						else -> ""
					}

					DownloadListHeader(
						text = { Text(label) },
						expanded = expanded,
						onExpandedChanged = { groupState[status] = !expanded }
					)
				}

				if (expanded) {
					items(
						items = items,
						key = { it.songId }
					) { item ->
						DownloadListItem(
							modifier = Modifier.animateItem(),
							download = item,
							onClick = { viewModel.selected = item },
							onDelete = { viewModel.deleteDownload(item) }
						)
					}
				}
			}
		}
	}

	ErrorSnackBar(
		error = (state as? UiState.Error)?.error,
		onClearError = viewModel::clearError
	)

	viewModel.selected?.let { selected ->
		DownloadSheet(
			onDismissRequest = { viewModel.selected = null },
			download = selected,
			onDelete = { viewModel.deleteDownload(selected) }
		)
	}
}
