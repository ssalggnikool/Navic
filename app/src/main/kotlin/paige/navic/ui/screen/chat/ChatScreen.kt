/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.chat

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Send
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.core.UiState
import paige.navic.ui.screen.chat.component.ChatInputBar
import paige.navic.ui.screen.chat.component.ChatMessageItem
import paige.navic.ui.screen.chat.viewmodel.ChatViewModel

@Composable
fun ChatScreen() {
	val viewModel = koinViewModel<ChatViewModel>()
	val state by viewModel.state.collectAsStateWithLifecycle()
	val inputState = viewModel.inputState
	val isSendingMessage by viewModel.isSendingMessage.collectAsStateWithLifecycle()

	val listState = rememberLazyListState()
	val isAtBottom by remember {
		derivedStateOf {
			val layoutInfo = listState.layoutInfo
			val totalItems = layoutInfo.totalItemsCount
			val lastIdx = layoutInfo.visibleItemsInfo.lastOrNull()?.index
			return@derivedStateOf (lastIdx == totalItems - 1) || totalItems == 0
		}
	}
	val scrollDown = {
		val index = state.data.orEmpty().asReversed().lastIndex.coerceAtLeast(0)
		listState.requestScrollToItem(index)
	}

	LaunchedEffect(state.data) {
		if (isAtBottom) scrollDown()
	}

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			if (event is ChatViewModel.Event.ScrollDown) scrollDown()
		}
	}

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_chat)) },
				actions = {
					if (state is UiState.Loading) {
						CircularProgressIndicator(
							modifier = Modifier.size(32.dp),
							strokeWidth = 6.dp
						)
					}
				}
			)
		},
		bottomBar = {
			BottomAppBar(
				containerColor = MaterialTheme.colorScheme.surface,
				contentPadding = PaddingValues(10.dp)
			) {
				ChatInputBar(
					modifier = Modifier.weight(1f),
					state = inputState,
					onSendAction = viewModel::sendMessage,
					enabled = !isSendingMessage
				)
				Spacer(Modifier.width(8.dp))
				FilledTonalIconButton(
					modifier = Modifier.size(56.dp),
					onClick = viewModel::sendMessage,
					enabled = !isSendingMessage && inputState.text.isNotEmpty()
				) {
					Icon(
						imageVector = Icons.Outlined.Send,
						contentDescription = stringResource(R.string.action_send_message)
					)
				}
			}
		}
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = innerPadding,
			state = listState
		) {
			if (state is UiState.Error) {
				item {
					Text(
						text = stringResource(R.string.info_chat_unsupported),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
			items(state.data.orEmpty().asReversed()) { message ->
				ChatMessageItem(message = message)
				HorizontalDivider()
			}
		}
	}
}
