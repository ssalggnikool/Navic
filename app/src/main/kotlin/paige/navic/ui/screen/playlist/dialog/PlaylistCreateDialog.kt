/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.playlist.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.model.DomainSong
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.PlaylistAdd
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.component.dialog.FormDialog
import paige.navic.ui.core.UiState
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.playlist.viewmodel.PlaylistCreateDialogViewModel

@Composable
fun PlaylistCreateDialog(
	onDismissRequest: () -> Unit,
	onRefresh: () -> Unit,
	songs: ImmutableList<DomainSong> = persistentListOf(),
	navigateAfterwards: Boolean = true
) {
	val viewModel = koinViewModel<PlaylistCreateDialogViewModel>(
		key = songs.joinToString { it.id },
		parameters = { parametersOf(songs) }
	)
	val backStack = LocalNavStack.current
	val state by viewModel.creationState.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is PlaylistCreateDialogViewModel.Event.Dismiss -> {
					onDismissRequest()
					onRefresh()
					if (navigateAfterwards) {
						if (backStack.contains(Screen.NowPlaying)) {
							backStack.remove(Screen.NowPlaying)
						}
						backStack.add(Screen.CollectionDetail(event.playlist.id, "playlists"))
					}
				}
			}
		}
	}

	FormDialog(
		onDismissRequest = onDismissRequest,
		icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
		title = { Text(stringResource(R.string.title_create_playlist)) },
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = viewModel::create,
				enabled = state !is UiState.Loading && viewModel.name.text.isNotBlank(),
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
				colors = SegmentedListButtonDefaults.primaryColors()
			) {
				if (state is UiState.Loading) {
					CircularProgressIndicator(Modifier.size(20.dp))
				}
				Text(stringResource(R.string.action_ok))
			}
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = onDismissRequest,
				enabled = state !is UiState.Loading,
				shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
			) {
				Text(stringResource(R.string.action_cancel))
			}
		},
		content = {
			(state as? UiState.Error)?.error?.let {
				SelectionContainer {
					Text("$it")
				}
			}
			TextField(
				state = viewModel.name,
				label = { Text(stringResource(R.string.option_playlist_name)) },
				lineLimits = TextFieldLineLimits.SingleLine
			)
		}
	)
}
