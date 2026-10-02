/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.entity.SyncActionType
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.manager.SyncManager
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.core.UiState
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete

enum class DeletionEndpoint(
	@StringRes val questionText: Int,
	@StringRes val deletedText: Int
) {
	PLAYLIST(R.string.title_delete_playlist, R.string.notice_deleted_playlist),
	SHARE(R.string.title_delete_share, R.string.notice_deleted_share)
}

class DeletionViewModel(
	private val syncManager: SyncManager,
	private val playlistDao: PlaylistDao,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	val state: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	fun delete(
		endpoint: DeletionEndpoint,
		id: String
	) {
		viewModelScope.launch {
			state.value = UiState.Loading()
			try {
				if (endpoint == DeletionEndpoint.SHARE) {
					sessionManager.api.deleteShare(id)
				} else {
					syncManager.enqueueAction(
						actionType = SyncActionType.DELETE_PLAYLIST,
						itemId = id
					)
					playlistDao.deletePlaylist(id)
				}
				state.value = UiState.Success(null)
				snackBarManager.notify(endpoint.deletedText)
				_events.send(Event.Dismiss)
			} catch (error: Exception) {
				state.value = UiState.Error(error = error)
			}
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}

@Composable
fun DeletionDialog(
	endpoint: DeletionEndpoint,
	id: String?,
	onIdClear: () -> Unit,
	onRefresh: () -> Unit
) {
	val viewModel = koinViewModel<DeletionViewModel>()
	val state by viewModel.state.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is DeletionViewModel.Event.Dismiss -> {
					onIdClear()
					onRefresh()
				}
			}
		}
	}

	id?.let {
		FormDialog(
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onIdClear()
				}
			},
			icon = { Icon(Icons.Outlined.Delete, null) },
			title = { Text(stringResource(endpoint.questionText)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = { viewModel.delete(endpoint, id) },
					enabled = state !is UiState.Loading,
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.errorColors()
				) {
					if (state is UiState.Loading) {
						CircularProgressIndicator(Modifier.size(20.dp))
					}
					Text(stringResource(R.string.action_delete))
				}
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = onIdClear,
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
				Text(
					stringResource(
						if (state !is UiState.Error)
							R.string.info_action_is_permanent
						else R.string.info_error
					)
				)
			}
		)
	}
}
