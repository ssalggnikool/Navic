/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.entity.SyncActionType
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.manager.SyncManager
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.core.UiState
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete

sealed interface DeletionTarget {
	data class Playlist(val id: String) : DeletionTarget
	data class Share(val id: String) : DeletionTarget
	data class Radio(val id: String) : DeletionTarget
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

	fun delete(target: DeletionTarget) {
		viewModelScope.launch {
			state.value = UiState.Loading()
			try {
				val noticeRes = when (target) {
					is DeletionTarget.Playlist -> {
						syncManager.enqueueAction(
							actionType = SyncActionType.DELETE_PLAYLIST,
							itemId = target.id
						)
						playlistDao.deletePlaylist(target.id)
						R.string.notice_deleted_playlist
					}

					is DeletionTarget.Share -> {
						sessionManager.api.deleteShare(target.id)
						R.string.notice_deleted_share
					}

					is DeletionTarget.Radio -> {
						sessionManager.api.deleteInternetRadioStation(target.id)
						R.string.notice_deleted_radio
					}
				}
				state.value = UiState.Success(null)
				snackBarManager.notify(noticeRes)
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
	target: DeletionTarget?,
	onDismissRequest: () -> Unit,
	onRefresh: (() -> Unit)? = null
) {
	val viewModel = koinViewModel<DeletionViewModel>()
	val state by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is DeletionViewModel.Event.Dismiss -> {
					onDismissRequest()
					onRefresh?.invoke()
				}
			}
		}
	}

	if (target != null) {
		val titleRes = when (target) {
			is DeletionTarget.Playlist -> R.string.title_delete_playlist
			is DeletionTarget.Share -> R.string.title_delete_share
			is DeletionTarget.Radio -> R.string.title_delete_radio
		}

		FormDialog(
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onDismissRequest()
				}
			},
			icon = { Icon(Icons.Outlined.Delete, null) },
			title = { Text(stringResource(titleRes)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = { viewModel.delete(target) },
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
					onClick = onDismissRequest,
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
