/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.ServerType

@Composable
fun LoginScreenFields(
	isBusy: Boolean,
	instanceState: TextFieldState,
	instanceError: Boolean,
	instanceFocusRequester: FocusRequester,
	authMethod: AuthMethod,
	serverType: ServerType?,
	onAuthMethodChange: (AuthMethod) -> Unit,
	usernameState: TextFieldState,
	usernameError: Boolean,
	usernameFocusRequester: FocusRequester,
	passwordState: TextFieldState,
	passwordError: Boolean,
	passwordFocusRequester: FocusRequester,
	onLogin: () -> Unit
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		OutlinedTextField(
			modifier = Modifier
				.height(70.dp)
				.padding(horizontal = 16.dp)
				.fillMaxWidth()
				.focusRequester(instanceFocusRequester),
			state = instanceState,
			isError = instanceError,
			label = { Text(stringResource(R.string.option_account_instance)) },
			lineLimits = TextFieldLineLimits.SingleLine,
			enabled = !isBusy,
			keyboardOptions = KeyboardOptions(
				autoCorrectEnabled = false,
				keyboardType = KeyboardType.Uri,
				imeAction = ImeAction.Next,
				showKeyboardOnFocus = true
			),
			onKeyboardAction = KeyboardActionHandler {
				if (serverType == ServerType.JELLYFIN || authMethod == AuthMethod.Token) {
					usernameFocusRequester.requestFocus()
				} else {
					passwordFocusRequester.requestFocus()
				}
			}
		)

		LoginScreenSuggestionChips(instanceState = instanceState)

		AnimatedVisibility(
			visible = serverType != ServerType.JELLYFIN
		) {
			SingleChoiceSegmentedButtonRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp)
			) {
				val methods = listOf(AuthMethod.Token, AuthMethod.ApiKey)
				methods.forEachIndexed { index, method ->
					SegmentedButton(
						shape = SegmentedButtonDefaults.itemShape(
							index = index,
							count = methods.size
						),
						onClick = { onAuthMethodChange(method) },
						selected = authMethod == method,
						enabled = !isBusy,
						label = {
							val label = when (method) {
								AuthMethod.Token -> stringResource(R.string.option_account_password)
								AuthMethod.ApiKey -> stringResource(R.string.option_account_api_key)
							}
							Text(label)
						}
					)
				}
			}
		}

		if (serverType == ServerType.JELLYFIN || authMethod == AuthMethod.Token) {
			OutlinedTextField(
				modifier = Modifier
					.height(70.dp)
					.padding(horizontal = 16.dp)
					.fillMaxWidth()
					.focusRequester(usernameFocusRequester)
					.semantics {
						contentType = ContentType.Username
					},
				state = usernameState,
				isError = usernameError,
				label = { Text(stringResource(R.string.option_account_username)) },
				lineLimits = TextFieldLineLimits.SingleLine,
				enabled = !isBusy,
				keyboardOptions = KeyboardOptions(
					autoCorrectEnabled = false,
					imeAction = ImeAction.Next,
					showKeyboardOnFocus = true
				),
				onKeyboardAction = KeyboardActionHandler {
					passwordFocusRequester.requestFocus()
				}
			)
		}

		OutlinedSecureTextField(
			modifier = Modifier
				.height(70.dp)
				.padding(horizontal = 16.dp)
				.fillMaxWidth()
				.focusRequester(passwordFocusRequester)
				.semantics {
					contentType = ContentType.Password
				},
			state = passwordState,
			isError = passwordError,
			label = {
				val label = when (authMethod) {
					AuthMethod.ApiKey -> stringResource(R.string.option_account_api_key)
					else -> stringResource(R.string.option_account_password)
				}
				Text(label)
			},
			enabled = !isBusy,
			keyboardOptions = KeyboardOptions(
				autoCorrectEnabled = false,
				keyboardType = KeyboardType.Password,
				imeAction = ImeAction.Go,
				showKeyboardOnFocus = true
			),
			onKeyboardAction = KeyboardActionHandler {
				onLogin()
			}
		)
	}
}
