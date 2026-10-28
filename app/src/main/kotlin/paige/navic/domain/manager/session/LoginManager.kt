/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import dev.zt64.subsonic.api.model.SubsonicErrorCode
import dev.zt64.subsonic.api.model.SubsonicException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.R
import paige.navic.domain.manager.runtime.NotificationIds
import paige.navic.domain.manager.runtime.NotificationManager
import paige.navic.domain.repository.DbRepository
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.ui.core.LoginEvent
import paige.navic.ui.core.LoginField
import paige.navic.ui.core.LoginFormState
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
	private val mainScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

	val loginState: StateFlow<LoginUiState>
		field = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	private val validationShown = MutableStateFlow(false)

	val formState: StateFlow<LoginFormState> = combine(
		snapshotFlow {
			Triple(
				instanceState.text.isBlank(),
				usernameState.text.isBlank(),
				passwordState.text.isBlank()
			)
		},
		validationShown
	) { (instanceBlank, usernameBlank, passwordBlank), shown ->
		LoginFormState(
			instanceError = shown && instanceBlank,
			usernameError = shown && usernameBlank,
			passwordError = shown && passwordBlank
		)
	}.stateIn(
		scope = mainScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = LoginFormState()
	)

	private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
	val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

	suspend fun isLocalNetworkInstance(): Boolean {
		val host = AddressUtil.extractHostFromUri(instanceState.text.toString())
			?: return false

		return withContext(Dispatchers.IO) { AddressUtil.isAddressLocal(host) }
	}

	fun login() {
		validationShown.value = true

		val url = instanceState.text.toString().trim().removeSuffix("/")
		val username = usernameState.text.toString()
		val password = passwordState.text.toString()

		val invalidField = when {
			url.isBlank() -> LoginField.Instance
			username.isBlank() -> LoginField.Username
			password.isBlank() -> LoginField.Password
			else -> null
		}

		if (invalidField != null) {
			_events.tryEmit(LoginEvent.ValidationFailed(invalidField))
			return
		}

		scope.launch {
			loginState.value = LoginUiState.Loading

			try {
				val normalizedUrl = if (url.startsWith("https://") || url.startsWith("http://")) {
					url
				} else {
					"https://$url"
				}

				try {
					sessionManager.login(normalizedUrl, username, password)
				} catch (e: SubsonicException) {
					if (e.code != SubsonicErrorCode.WRONG_USERNAME_OR_PASSWORD) {
						throw e
					}
					loginState.value = LoginUiState.Idle
					_events.tryEmit(LoginEvent.InvalidCredentials)
					return@launch
				}

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
					_events.tryEmit(LoginEvent.Success)
				}.onFailure { e ->
					loginState.value = LoginUiState.Error(e as? Exception ?: Exception(e))
				}

			} catch (e: Exception) {
				loginState.value = LoginUiState.Error(e)
			}
		}
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
