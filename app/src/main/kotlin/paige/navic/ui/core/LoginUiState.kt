/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

sealed class LoginUiState {
	object Idle : LoginUiState()
	object Loading : LoginUiState()
	object Success : LoginUiState()
	data class Syncing(val progress: Float, val message: Int) : LoginUiState()
	data class Error(val error: Exception) : LoginUiState()
}

data class LoginFormState(
	val instanceError: Boolean = false,
	val usernameError: Boolean = false,
	val passwordError: Boolean = false
)

enum class LoginField {
	Instance,
	Username,
	Password
}

sealed class LoginEvent {
	object Success : LoginEvent()
	object InvalidCredentials : LoginEvent()
	data class ValidationFailed(val field: LoginField) : LoginEvent()
}
