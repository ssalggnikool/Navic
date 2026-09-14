package paige.navic.ui.screens.share.dialogs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.notice_copied
import navic.composeapp.generated.resources.notice_expiry
import navic.composeapp.generated.resources.option_share_expires
import navic.composeapp.generated.resources.title_create_share
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalSnackBarState
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.DurationPicker
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.core.UiState
import paige.navic.ui.screens.share.viewmodels.ShareDialogViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareDialog(
	id: String?,
	onIdClear: () -> Unit,
	expiry: Duration?,
	onExpiryChange: (expiry: Duration?) -> Unit
) {

	val viewModel = koinViewModel<ShareDialogViewModel>()

	// There is not an elegant cross-platform way of making a ClipEntry yet this is deprecated lmao
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	val snackBarState = LocalSnackBarState.current
	val state by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(state) {
		if (state is UiState.Success && id != null) {
			viewModel.viewModelScope.launch {
				val link = (state as? UiState.Success<String?>)?.data
					?: return@launch
				onIdClear()
				clipboard.setText(AnnotatedString(link))
				snackBarState.showSnackbar(
					message = buildString {
						append(getString(Res.string.notice_copied))
						expiry?.let {
							append(
								"\n" + getString(
									Res.string.notice_expiry, expiry.toString()
								)
							)
						}
					}
				)
			}
		}
	}

	id?.let {
		FormDialog(
			icon = { Icon(Icons.Outlined.Share, null) },
			title = { Text(stringResource(Res.string.title_create_share)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = { viewModel.share(id, expiry) },
					enabled = state !is UiState.Loading,
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.primaryColors()
				) {
					if (state is UiState.Loading) {
						CircularProgressIndicator(Modifier.size(20.dp))
					}
					Text(stringResource(Res.string.action_share))
				}
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = onIdClear,
					shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
				) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onIdClear()
				}
			}
		) {
			Spacer(Modifier.height(12.dp))
			(state as? UiState.Error)?.error?.let {
				SelectionContainer {
					Text("$it")
				}
			}

			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
			) {
				val checked = expiry != null
				val interactionSource = remember { MutableInteractionSource() }

				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = 0,
						count = if (checked) 2 else 1
					),
					onClick = { onExpiryChange(if (!checked) 1.hours else null) },
					enabled = state !is UiState.Loading,
					interactionSource = interactionSource,
					content = { Text(stringResource(Res.string.option_share_expires)) },
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
						onClick = {},
						enabled = false,
						contentPadding = PaddingValues(10.dp),
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = 2
						)
					) {
						DurationPicker(
							duration = expiry,
							onDurationChange = onExpiryChange,
							enabled = state !is UiState.Loading,
						)
					}
				}
			}
		}
	}
}
