/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.snackbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.di.LocalSnackBarState
import paige.navic.ui.component.common.ErrorCodeBlock
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.component.dialog.FormDialog
import paige.navic.util.Logger

@Composable
fun ErrorSnackBar(
	error: Throwable?,
	onClearError: () -> Unit
) {
	if (error == null) return

	val snackBarState = LocalSnackBarState.current
	val resources = LocalResources.current
	var visible by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(error) {
		val result = snackBarState.showSnackbar(
			message = resources.getString(R.string.info_error),
			actionLabel = resources.getString(R.string.info_error_show),
			duration = SnackbarDuration.Long
		)
		if (result == SnackbarResult.ActionPerformed) {
			visible = true
			Logger.e("ErrorSnackBar", "Printing stack trace for error", error)
		} else {
			onClearError()
		}
	}

	if (!visible) return

	FormDialog(
		onDismissRequest = {
			visible = false
			onClearError()
		},
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					visible = false
					onClearError()
				},
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 1)
			) {
				Text(stringResource(R.string.action_ok))
			}
		}
	) {
		ErrorCodeBlock(error)
	}
}
