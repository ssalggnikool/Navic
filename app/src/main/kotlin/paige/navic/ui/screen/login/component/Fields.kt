/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun LoginScreenFields(
	isBusy: Boolean,
	instanceState: TextFieldState,
	instanceError: Boolean,
	instanceFocusRequester: FocusRequester,
	usernameState: TextFieldState,
	usernameError: Boolean,
	usernameFocusRequester: FocusRequester,
	passwordState: TextFieldState,
	passwordError: Boolean,
	passwordFocusRequester: FocusRequester,
	onLogin: () -> Unit
) {
	OutlinedTextField(
		modifier = Modifier
			.heightIn(min = 60.dp)
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
			usernameFocusRequester.requestFocus()
		}
	)

	LoginScreenSuggestionChips(instanceState = instanceState)

	OutlinedTextField(
		modifier = Modifier
			.heightIn(min = 60.dp)
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

	OutlinedSecureTextField(
		modifier = Modifier
			.heightIn(min = 60.dp)
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
			.focusRequester(passwordFocusRequester)
			.semantics {
				contentType = ContentType.Password
			},
		state = passwordState,
		isError = passwordError,
		label = { Text(stringResource(R.string.option_account_password)) },
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
