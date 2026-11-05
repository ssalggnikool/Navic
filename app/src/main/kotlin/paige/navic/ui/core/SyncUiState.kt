/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

sealed class SyncUiState {
	object Idle : SyncUiState()
	data class Syncing(val progress: Float, val message: Int) : SyncUiState()
	data class Error(val error: Exception) : SyncUiState()
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
	object Loading: LoginEvent()
	object Success : LoginEvent()
	object InvalidCredentials : LoginEvent()
	data class ValidationFailed(val field: LoginField) : LoginEvent()
	data class Error(val error: Exception) : LoginEvent()
}
