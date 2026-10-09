/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Error

@Composable
fun SettingsInputItem(
	value: String,
	onValueChanged: (String) -> Unit,
	onValidate: (String) -> Boolean = { true },
	defaultValue: String,
	enabled: Boolean = true,
	shapes: ListItemShapes,
	leadingContent: @Composable (() -> Unit)? = null,
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	var inputDialogOpen by rememberSaveable { mutableStateOf(false) }
	val textFieldState = rememberTextFieldState(initialText = value)
	val inputValidated = remember(textFieldState.text) {
		onValidate(textFieldState.text.toString())
	}

	SettingsNavItem(
		onClick = { inputDialogOpen = true },
		enabled = enabled,
		shapes = shapes,
		leadingContent = leadingContent,
		supportingContent = supportingContent,
		content = content
	)

	if (inputDialogOpen) {
		AlertDialog(
			onDismissRequest = { inputDialogOpen = false },
			title = content,
			text = {
				Column(
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					OutlinedTextField(
						state = textFieldState,
						modifier = Modifier.fillMaxWidth(),
						isError = !inputValidated,
						trailingIcon = if (!inputValidated) {
							{
								// why
								Box(
									modifier = Modifier.width(55.dp),
									contentAlignment = Alignment.Center
								) {
									Icon(
										imageVector = Icons.Outlined.Error,
										contentDescription = null
									)
								}
							}
						} else {
							null
						}
					)
					val resetEnabled = textFieldState.text.toString() != defaultValue
					Text(
						text = stringResource(R.string.action_reset_to_default),
						color = if (resetEnabled)
							MaterialTheme.colorScheme.primary
						else MaterialTheme.colorScheme.primary.copy(alpha = .5f),
						modifier = Modifier.clickable(enabled = resetEnabled) {
							textFieldState.setTextAndPlaceCursorAtEnd(defaultValue)
						}
					)
				}
			},
			dismissButton = {
				TextButton(onClick = { inputDialogOpen = false }) {
					Text(stringResource(R.string.action_cancel))
				}
			},
			confirmButton = {
				Button(
					onClick = {
						inputDialogOpen = false
						onValueChanged(textFieldState.text.toString())
					},
					enabled = inputValidated
				) {
					Text(stringResource(R.string.action_ok))
				}
			}
		)
	}
}
