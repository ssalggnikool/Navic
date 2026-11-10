/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.domain.repository.DbRepository
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.ui.core.LoginEvent
import paige.navic.ui.core.LoginField
import paige.navic.ui.core.LoginFormState
import paige.navic.ui.util.AddressUtil
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class LoginManager(
	private val repository: DbRepository,
	private val sessionManager: SessionManager,
	private val syncManager: SyncManager,
	private val syncCoordinator: SyncCoordinator,
	private val downloadManager: DownloadManager
) {
	val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val mainScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	var authMethod by mutableStateOf(AuthMethod.Token)

	var supportsApiKeyAuthentication by mutableStateOf(false)
		private set

	fun selectAuthMethod(method: AuthMethod) {
		if (authMethod != method) {
			passwordState.clearText()
		}
		authMethod = method
	}

	private val validationShown = MutableStateFlow(false)

	val formState: StateFlow<LoginFormState> = combine(
		snapshotFlow { instanceState.text.isBlank() },
		snapshotFlow { usernameState.text.isBlank() },
		snapshotFlow { passwordState.text.isBlank() },
		snapshotFlow { authMethod },
		validationShown
	) { instanceBlank, usernameBlank, secretBlank, method, shown ->
		LoginFormState(
			instanceError = shown && instanceBlank,
			usernameError = shown && method == AuthMethod.Token && usernameBlank,
			passwordError = shown && secretBlank
		)
	}.stateIn(
		scope = mainScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = LoginFormState()
	)

	init {
		mainScope.launch {
			snapshotFlow { instanceState.text.toString() }
				.debounce(800.milliseconds)
				.collectLatest { raw ->
					val supported = raw.isNotBlank() && runCatching {
						sessionManager.supportsApiKeyAuthentication(normalizeUrl(raw))
					}.getOrDefault(false)

					supportsApiKeyAuthentication = supported

					if (!supported && authMethod == AuthMethod.ApiKey) {
						authMethod = AuthMethod.Token
						passwordState.clearText()
					}
				}
		}
	}

	private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
	val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

	private fun normalizeUrl(url: String): String {
		val trimmed = url.trim().removeSuffix("/")
		return if (trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
			trimmed
		} else {
			"https://$trimmed"
		}
	}

	suspend fun isLocalNetworkInstance(): Boolean {
		val host = AddressUtil.extractHostFromUri(instanceState.text.toString())
			?: return false

		return try {
			withContext(Dispatchers.IO) {
				AddressUtil.isAddressLocal(host)
			}
		} catch (_: Exception) {
			false
		}
	}

	fun login() {
		validationShown.value = true

		val method = authMethod
		val url = instanceState.text.toString().trim().removeSuffix("/")
		val username = usernameState.text.toString()
		val secret = passwordState.text.toString().let {
			if (method == AuthMethod.ApiKey) it.trim() else it
		}

		val invalidField = when {
			url.isBlank() -> LoginField.Instance
			method == AuthMethod.Token && username.isBlank() -> LoginField.Username
			secret.isBlank() -> LoginField.Password
			else -> null
		}

		if (invalidField != null) {
			_events.tryEmit(LoginEvent.ValidationFailed(invalidField))
			return
		}

		scope.launch {
			val normalizedUrl = normalizeUrl(url)

			try {
				_events.tryEmit(LoginEvent.Loading)
				if (method == AuthMethod.ApiKey) {
					sessionManager.login(normalizedUrl, secret)
				} else {
					sessionManager.login(normalizedUrl, username, secret)
				}
			} catch (e: InvalidCredentialsException) {
				_events.tryEmit(LoginEvent.InvalidCredentials)
				return@launch
			} catch (e: Exception) {
				_events.tryEmit(LoginEvent.Error(e))
				return@launch
			}

			syncManager.triggerManualSync()
			_events.tryEmit(LoginEvent.Success)
		}
	}

	fun logout() {
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
