/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import paige.navic.domain.manager.runtime.StorageManager
import paige.navic.domain.manager.session.account.Account
import paige.navic.domain.manager.session.account.AccountStore
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.ServerType
import paige.navic.domain.server.UserCredentials
import paige.navic.preferences.AccountSettings
import paige.navic.preferences.PreferenceManager
import paige.navic.ui.core.LoginEvent
import paige.navic.ui.core.LoginField
import paige.navic.ui.core.LoginFormState
import paige.navic.ui.util.AddressUtil
import paige.navic.util.AppRestarter

class LoginManager(
	private val sessionManager: SessionManager,
	private val syncManager: SyncManager,
	private val downloadManager: DownloadManager,
	private val storageManager: StorageManager,
	private val accountStore: AccountStore,
	private val accountSettings: AccountSettings,
	private val preferenceManager: PreferenceManager,
	private val appRestarter: AppRestarter
) {
	val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private val mainScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	var serverType by mutableStateOf<ServerType?>(null)

	var authMethod by mutableStateOf(AuthMethod.Token)


	fun selectServerType(type: ServerType) {
		if (serverType != type) {
			passwordState.clearText()
		}
		serverType = type
	}

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
		snapshotFlow { serverType to authMethod },
		validationShown
	) { instanceBlank, usernameBlank, secretBlank, method, shown ->
		val (type, auth) = method
		val needsUsername = type == ServerType.JELLYFIN ||
			(type != null && auth == AuthMethod.Token)
		LoginFormState(
			instanceError = shown && instanceBlank,
			usernameError = shown && needsUsername && usernameBlank,
			passwordError = shown && secretBlank
		)
	}.stateIn(
		scope = mainScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = LoginFormState()
	)


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

	fun fillFor(account: Account) {
		instanceState.setTextAndPlaceCursorAtEnd(account.baseUrl)
		usernameState.setTextAndPlaceCursorAtEnd(account.username.orEmpty())
		passwordState.clearText()
		account.serverType?.let(::selectServerType)
		selectAuthMethod(account.authMethod)
	}

	fun login() {
		validationShown.value = true

		val type = serverType ?: return
		val method = authMethod
		val url = instanceState.text.toString().trim().removeSuffix("/")
		val username = usernameState.text.toString()
		val secret = passwordState.text.toString().let {
			if (type != ServerType.JELLYFIN && method == AuthMethod.ApiKey) it.trim() else it
		}

		val requiresUsername = type == ServerType.JELLYFIN || method == AuthMethod.Token
		val invalidField = when {
			url.isBlank() -> LoginField.Instance
			requiresUsername && username.isBlank() -> LoginField.Username
			secret.isBlank() -> LoginField.Password
			else -> null
		}

		if (invalidField != null) {
			_events.tryEmit(LoginEvent.ValidationFailed(invalidField))
			return
		}

		scope.launch {
			val normalizedUrl = normalizeUrl(url)
			val previousAccountId = sessionManager.activeAccountId

			val account = try {
				_events.tryEmit(LoginEvent.Loading)
				val credentials = when {
					type == ServerType.JELLYFIN ->
						UserCredentials.Jellyfin(username, secret)

					method == AuthMethod.ApiKey ->
						UserCredentials.ApiKey(secret)

					else ->
						UserCredentials.Password(username, secret)
				}
				sessionManager.login(normalizedUrl, credentials)
			} catch (_: InvalidCredentialsException) {
				_events.tryEmit(LoginEvent.InvalidCredentials)
				return@launch
			} catch (e: Exception) {
				_events.tryEmit(LoginEvent.Error(e))
				return@launch
			}

			// a different account means different databases and directories, which
			// are only resolved on startup
			if (account.id != previousAccountId) {
				activate(account.id)
				return@launch
			}

			syncManager.triggerManualSync()
			_events.tryEmit(LoginEvent.Success)
		}
	}

	fun switchAccount(accountId: String) {
		if (accountId == sessionManager.activeAccountId) return

		scope.launch {
			activate(accountId)
		}
	}

	fun removeAccount(accountId: String) {
		val isActive = accountId == sessionManager.activeAccountId

		scope.launch {
			if (isActive) {
				downloadManager.cancelAllActiveDownloadsAndWait()
			}

			storageManager.deleteAccountData(accountId)
			accountSettings.clearAccount(accountStore.keyPrefixFor(accountId))
			accountStore.queueDatabaseDeletion(accountId)
			sessionManager.removeAccount(accountId)

			if (isActive) {
				preferenceManager.lastFullSyncTime = 0
				appRestarter.restart()
			}
		}
	}

	private suspend fun activate(accountId: String) {
		downloadManager.cancelAllActiveDownloadsAndWait()
		preferenceManager.lastFullSyncTime = 0
		sessionManager.setActiveAccount(accountId)
		appRestarter.restart()
	}
}
