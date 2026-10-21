/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.domain.repository.DbRepository
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.ui.core.LoginUiState
import paige.navic.ui.util.AddressUtil

class LoginManager(
	private val repository: DbRepository,
	private val sessionManager: SessionManager,
	private val syncManager: SyncManager,
	private val syncCoordinator: SyncCoordinator,
	private val notificationManager: NotificationManager,
	private val downloadManager: DownloadManager,
	private val context: Context
) {
	val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	val loginState: StateFlow<LoginUiState>
		field = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	var instanceError by mutableStateOf(false)
		private set
	var usernameError by mutableStateOf(false)
		private set
	var passwordError by mutableStateOf(false)
		private set

	fun validateInstance() {
		instanceError = instanceState.text.isBlank()
	}

	fun validateUsername() {
		usernameError = usernameState.text.isBlank()
	}

	fun validatePassword() {
		passwordError = passwordState.text.isBlank()
	}

	fun validateStuff(): Boolean {
		validateInstance()
		validateUsername()
		validatePassword()
		return !instanceError && !usernameError && !passwordError
	}

	fun isLocalNetworkInstance(): Boolean {
		val host = AddressUtil.extractHostFromUri(instanceState.text.toString())

		return if (host != null) {
			AddressUtil.isAddressLocal(host)
		} else {
			false
		}
	}

	init {
		loadUser()
	}

	fun loadUser() {
		scope.launch {
			if (sessionManager.isLoggedIn.value) {
				loginState.value = LoginUiState.Success
			} else {
				loginState.value = LoginUiState.Idle
			}
		}
	}

	fun login(): Boolean {
		if (!validateStuff()) return false

		scope.launch {
			loginState.value = LoginUiState.Loading

			try {
				val rawUrl = instanceState.text.toString().trim().removeSuffix("/")
				val url = if (rawUrl.startsWith("https://") || rawUrl.startsWith("http://")) {
					rawUrl
				} else {
					"https://$rawUrl"
				}

				sessionManager.login(
					url,
					usernameState.text.toString(),
					passwordState.text.toString()
				)

				syncManager.startPeriodicSync(syncImmediately = false)

				val result = try {
					syncCoordinator.run(force = true) { progress, message ->
						loginState.value = LoginUiState.Syncing(progress, message)

						notificationManager.showProgressNotification(
							id = NotificationIds.SYNC_LIBRARY,
							title = contextTitle(progress),
							message = contextString(message),
							progress = progress,
							indeterminate = progress <= 0f
						)
					}
				} finally {
					notificationManager.cancelNotification(NotificationIds.SYNC_LIBRARY)
				}

				result.onSuccess {
					loginState.value = LoginUiState.Success
				}.onFailure { e ->
					loginState.value = LoginUiState.Error(e as? Exception ?: Exception(e))
				}

			} catch (e: Exception) {
				loginState.value = LoginUiState.Error(e)
			}
		}

		return true
	}

	private fun contextTitle(progress: Float): String {
		val title = context.getString(R.string.title_sync_control)
		return "$title (${(progress * 100).toInt()}%)"
	}

	private fun contextString(@StringRes id: Int): String {
		return context.getString(id)
	}

	fun logout() {
		loginState.value = LoginUiState.Idle
		syncManager.stopPeriodicSync()
		downloadManager.cancelAllActiveDownloads()
		sessionManager.logout()

		scope.launch {
			syncManager.stopPeriodicSyncAndWait()
			syncCoordinator.awaitIdle()
			repository.removeEverything()
		}
	}
}
