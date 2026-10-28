/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.dialog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.CoverArtHost
import paige.navic.domain.model.settings.CoverArtProvider
import paige.navic.ui.component.sheet.ModalBottomSheet
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.DragHandle
import paige.navic.ui.icons.outlined.Edit
import paige.navic.ui.screen.settings.viewmodel.CoverArtProvidersViewModel
import paige.navic.ui.util.DraggableListState
import paige.navic.ui.util.dragHandle
import paige.navic.ui.util.draggableItems
import paige.navic.ui.util.rememberDraggableListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverArtProvidersSheet(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val viewModel = koinViewModel<CoverArtProvidersViewModel>()
	val preferenceManager = koinInject<PreferenceManager>()
	val config by viewModel.state.collectAsStateWithLifecycle()
	val haptic = LocalHapticFeedback.current
	var keyDialogHost by remember { mutableStateOf<CoverArtHost?>(null) }

	val draggableState = rememberDraggableListState { from, to ->
		viewModel.move(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetTitle = stringResource(R.string.title_cover_art_providers)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = stringResource(R.string.title_cover_art_providers),
				style = MaterialTheme.typography.titleLarge
			)
			Text(
				text = stringResource(R.string.subtitle_cover_art_providers),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(Modifier.height(8.dp))

			LazyColumn(
				modifier = Modifier
					.fillMaxWidth()
					.wrapContentHeight(),
				state = draggableState.listState,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				draggableItems(
					state = draggableState,
					items = config.providers,
					key = { it.host }
				) { provider, isDragging ->
					ProviderRow(
						provider = provider,
						keySet = preferenceManager.discordCoverArtApiKeyProviders[provider.host.name] != null,
						state = draggableState,
						isDragging = isDragging,
						onToggleEnabled = { viewModel.toggleEnabled(provider.host) },
						onEditKey = if (provider.host.needsApiKey) {
							{ keyDialogHost = provider.host }
						} else {
							null
						}
					)
				}
			}

			Spacer(Modifier.height(8.dp))
		}
	}

	if (keyDialogHost != null) {
		keyDialogHost?.let {
			KeyDialog(
				title = stringResource(R.string.action_edit_api_key, it.displayName),
				value = preferenceManager.discordCoverArtApiKeyProviders[it.name] ?: "",
				onDismiss = { keyDialogHost = null },
				onConfirm = { value ->
					if (value.isBlank()) return@KeyDialog

					val setting = preferenceManager.discordCoverArtApiKeyProviders.toMutableMap()
					setting[it.name] = value
					preferenceManager.discordCoverArtApiKeyProviders = setting
				}
			)
		}
	}
}

@Composable
private fun ProviderRow(
	provider: CoverArtProvider,
	keySet: Boolean,
	state: DraggableListState,
	isDragging: Boolean,
	onToggleEnabled: () -> Unit,
	onEditKey: (() -> Unit)?
) {
	val elevation by animateDpAsState(
		targetValue = if (isDragging) 4.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)

	Surface(
		shadowElevation = elevation,
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.large,
		color = MaterialTheme.colorScheme.surfaceContainer
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				checked = provider.enabled,
				onCheckedChange = { onToggleEnabled() }
			)

			Column(modifier = Modifier.weight(1f)) {
				Text(provider.host.displayName)
				if (onEditKey != null && !keySet) {
					Text(
						text = stringResource(R.string.subtitle_cover_art_key_required),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.error
					)
				}
			}

			if (onEditKey != null) {
				IconButton(onClick = onEditKey) {
					Icon(
						imageVector = Icons.Outlined.Edit,
						contentDescription = stringResource(R.string.action_edit)
					)
				}
			}

			IconButton(
				modifier = Modifier.dragHandle(state = state, key = provider.host),
				onClick = {}
			) {
				Icon(
					imageVector = Icons.Outlined.DragHandle,
					contentDescription = stringResource(R.string.action_reorder)
				)
			}
		}
	}
}

@Composable
private fun KeyDialog(
	title: String,
	value: String,
	onDismiss: () -> Unit,
	onConfirm: (String) -> Unit
) {
	var text by remember(value) { mutableStateOf(value) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = {
			OutlinedTextField(
				value = text,
				onValueChange = { text = it },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true
			)
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.action_cancel))
			}
		},
		confirmButton = {
			Button(
				onClick = {
					onConfirm(text.trim())
					onDismiss()
				}
			) {
				Text(stringResource(R.string.action_ok))
			}
		}
	)
}
