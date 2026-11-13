/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.di.LocalSnackBarState
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.isNavidrome
import paige.navic.ui.component.common.DurationPicker
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.sheet.ModalBottomSheet
import paige.navic.ui.core.UiState
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Share
import paige.navic.ui.screen.share.viewmodel.ShareDialogViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
	id: String?,
	showDialog: Boolean = id != null,
	onDismissRequest: () -> Unit
) {
	val viewModel = koinViewModel<ShareDialogViewModel>()
	val sessionManager = koinInject<SessionManager>()

	val isNavidrome = sessionManager.isNavidrome()

	// There is not an elegant cross-platform way of making a ClipEntry yet this is deprecated lmao
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	val resources = LocalResources.current
	val snackBarState = LocalSnackBarState.current
	val state by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(state) {
		if (state is UiState.Success && id != null) {
			viewModel.viewModelScope.launch {
				val link = (state as? UiState.Success<String?>)?.data
					?: return@launch
				onDismissRequest()
				clipboard.setText(AnnotatedString(link))
				snackBarState.showSnackbar(
					message = resources.getString(R.string.notice_copied)
				)
			}
		}
	}

	if (showDialog && id != null) {
		ModalBottomSheet(
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onDismissRequest()
				}
			}
		) {
			Column(
				modifier = Modifier.padding(24.dp)
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(8.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Icon(
						modifier = Modifier.size(32.dp),
						imageVector = Icons.Outlined.Share,
						contentDescription = null
					)
					Text(
						style = MaterialTheme.typography.headlineMedium,
						text = stringResource(R.string.title_create_share)
					)
				}
				Spacer(Modifier.height(16.dp))

				(state as? UiState.Error)?.error?.let {
					SelectionContainer {
						Text("$it")
					}
				}

				ShareOptions(
					isNavidrome,
					state,
					viewModel,
					id
				)
			}
		}
	}
}

@Composable
private fun ShareOptions(
	isNavidrome: Boolean,
	state: UiState<String?>,
	viewModel: ShareDialogViewModel,
	id: String,
) {
	var expiry by remember(id) { mutableStateOf<Duration?>(null) }
	var description by remember(id) { mutableStateOf("") }
	var downloadable by remember(id) { mutableStateOf(false) }

	Column(
		modifier = Modifier.navigationBarsPadding(),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		OutlinedTextField(
			value = description,
			onValueChange = { description = it },
			modifier = Modifier.fillMaxWidth(),
			enabled = state !is UiState.Loading,
			label = { Text(stringResource(R.string.option_share_description)) },
			singleLine = true
		)

		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
		) {
			val checked = expiry != null
			val count by remember {
				derivedStateOf {
					val baseCount = if (checked) 2 else 1

					if (isNavidrome) {
						baseCount + 1
					} else {
						baseCount
					}
				}
			}

			if (isNavidrome) {
				val downloadableInteractionSource = remember { MutableInteractionSource() }

				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = 0,
						count = count
					),
					onClick = { downloadable = !downloadable },
					enabled = state !is UiState.Loading,
					interactionSource = downloadableInteractionSource,
					content = { Text(stringResource(R.string.option_share_downloadable)) },
					contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
					trailingContent = {
						Switch(
							modifier = Modifier.padding(start = 4.dp),
							checked = downloadable,
							onCheckedChange = null,
							enabled = state !is UiState.Loading,
							interactionSource = downloadableInteractionSource
						)
					}
				)
			}

			val interactionSource = remember { MutableInteractionSource() }

			SegmentedListItem(
				shapes = SegmentedListItemDefaults.segmentedShapes(
					index = if (!isNavidrome) 0 else 1,
					count = count
				),
				onClick = { expiry = if (!checked) 1.hours else null },
				enabled = state !is UiState.Loading,
				interactionSource = interactionSource,
				content = { Text(stringResource(R.string.option_share_expires)) },
				contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
				trailingContent = {
					Switch(
						modifier = Modifier.padding(start = 4.dp),
						checked = checked,
						onCheckedChange = null,
						enabled = state !is UiState.Loading,
						interactionSource = interactionSource
					)
				}
			)

			if (checked) {
				SegmentedListItem(
					enabled = false,
					contentPadding = PaddingValues(10.dp),
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = if (!isNavidrome) 1 else 2,
						count = count
					)
				) {
					DurationPicker(
						modifier = Modifier.fillMaxWidth(),
						duration = expiry ?: 1.hours,
						onDurationChange = { expiry = it },
						enabled = state !is UiState.Loading,
					)
				}
			}
		}

		SegmentedListButton(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				viewModel.share(
					id = id,
					expiry = expiry,
					description = description.trim().ifBlank { null },
					downloadable = downloadable
				)
			},
			enabled = state !is UiState.Loading,
			shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 1),
			colors = SegmentedListButtonDefaults.primaryColors()
		) {
			if (state is UiState.Loading) {
				CircularProgressIndicator(Modifier.size(20.dp))
			}
			Text(stringResource(R.string.action_share))
		}
	}
}
