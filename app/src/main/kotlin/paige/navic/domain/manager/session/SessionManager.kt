/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.model.DomainUser
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.ChatServer
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.MusicServerFactory
import paige.navic.domain.server.PlaylistCoverServer
import paige.navic.domain.server.RadioServer
import paige.navic.domain.server.ServerCredentials
import paige.navic.domain.server.ServerExtension
import paige.navic.domain.server.ServerInfo
import paige.navic.domain.server.ShareServer
import paige.navic.preferences.PreferenceManager
import paige.navic.util.Logger
import paige.navic.util.TTLLruCache
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

sealed interface SessionState {
	data object Unauthenticated : SessionState
	data object Restoring : SessionState
	data class Authenticated(
		val currentUser: DomainUser?,
		val serverInfo: ServerInfo?
	) : SessionState
}

class SessionManager(
	private val accountStore: AccountStore,
	private val preferenceManager: PreferenceManager,
	private val serverFactory: MusicServerFactory
) {
	val ttlLruCache = TTLLruCache<String, String>(
		maxSize = 500,
		defaultTtlMillis = 30.minutes.toLong(DurationUnit.MILLISECONDS)
	)

	private companion object {
		const val TAG = "SessionManager"
	}

	val accounts: StateFlow<List<Account>> = accountStore.accounts

	val activeAccountId: String?
		get() = accountStore.activeAccountId

	private val hasStoredCredentials: Boolean
		get() = accountStore.activeAccount?.hasCredentials == true

	private val _state = MutableStateFlow(
		if (hasStoredCredentials) SessionState.Restoring else SessionState.Unauthenticated
	)
	val sessionState: StateFlow<SessionState> = _state.asStateFlow()

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	val username: String
		get() = accountStore.activeAccount?.username.orEmpty()

	val instanceUrl: String
		get() = accountStore.activeAccount?.baseUrl.orEmpty()

	val authMethod: AuthMethod
		get() = accountStore.activeAccount?.authMethod ?: AuthMethod.Token

	private fun createClient(account: Account?): MusicServer = serverFactory.create(
		baseUrl = account?.baseUrl.orEmpty(),
		credentials = account?.credentials() ?: ServerCredentials.Token("", "")
	)

	var api: MusicServer = createClient(accountStore.activeAccount)
		private set

	val chat: ChatServer?
		get() = api as? ChatServer

	val shares: ShareServer?
		get() = api as? ShareServer

	val radio: RadioServer?
		get() = api as? RadioServer

	val playlistCovers: PlaylistCoverServer?
		get() = api as? PlaylistCoverServer

	init {
		val account = accountStore.activeAccount
		if (account != null && account.hasCredentials) {
			scope.launch {
				try {
					restore(account)
				} catch (e: InvalidCredentialsException) {
					// keep the account so its cache and downloads survive a rejected login
					Logger.e(TAG, "Credentials invalid, signing out", e)
					signOut()
				} catch (e: Exception) {
					Logger.w(TAG, "Failed to reach server during restoration, treating as offline authenticated", e)
					_state.value = SessionState.Authenticated(
						currentUser = null,
						serverInfo = null
					)
				}
			}
		}
	}

	private suspend fun restore(account: Account) {
		val server = serverFactory.create(
			baseUrl = account.baseUrl,
			credentials = account.credentials()
		)
		val serverInfo = server.serverInfo()
		Logger.i(TAG, "Ping result: $serverInfo")
		val user = server.currentUser()

		api = server
		_state.value = SessionState.Authenticated(
			currentUser = user,
			serverInfo = serverInfo
		)
	}

	@Throws(InvalidCredentialsException::class)
	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	): Account = login(
		instanceUrl = instanceUrl,
		credentials = ServerCredentials.Token(username = username, password = password)
	)

	@Throws(InvalidCredentialsException::class)
	suspend fun login(
		instanceUrl: String,
		apiKey: String
	): Account = login(
		instanceUrl = instanceUrl,
		credentials = ServerCredentials.ApiKey(apiKey = apiKey)
	)

	suspend fun loginJellyfin(
		instanceUrl: String,
		credentials: ServerCredentials.Jellyfin
	): Account = login(
		instanceUrl = instanceUrl,
		credentials = credentials
	)

	private suspend fun login(instanceUrl: String, credentials: ServerCredentials): Account {
		val server = serverFactory.create(
			baseUrl = instanceUrl,
			credentials = credentials
		)

		val serverInfo = server.serverInfo()
		Logger.i(TAG, "Ping result: $serverInfo")

		val user = server.currentUser()

		val account = accountStore.upsert(
			Account(
				id = UUID.randomUUID().toString(),
				baseUrl = instanceUrl,
				authMethod = when (credentials) {
					is ServerCredentials.Token -> AuthMethod.Token
					is ServerCredentials.ApiKey -> AuthMethod.ApiKey
					is ServerCredentials.Jellyfin -> AuthMethod.Jellyfin
				},
				username = when (credentials) {
					is ServerCredentials.Token -> credentials.username
					is ServerCredentials.ApiKey -> user.username
					is ServerCredentials.Jellyfin -> credentials.username
				},
				password = (credentials as? ServerCredentials.Token)?.password.orEmpty(),
				apiKey = (credentials as? ServerCredentials.ApiKey)?.apiKey.orEmpty(),
				jellyfinToken = (credentials as? ServerCredentials.Jellyfin)?.accessToken.orEmpty(),
				jellyfinUserId = (credentials as? ServerCredentials.Jellyfin)?.userId.orEmpty()
			)
		)

		// never change the active account here: a different account means different
		// databases and directories, which the caller applies by switching and restarting
		if (account.id == accountStore.activeAccountId) {
			api = server
			_state.value = SessionState.Authenticated(
				currentUser = user,
				serverInfo = serverInfo
			)
		}

		return account
	}

	fun signOut() {
		ttlLruCache.clear()
		_state.value = SessionState.Unauthenticated
	}

	/**
	 * Points the session at another saved account. Data paths only change on the
	 * next process start, so callers restart the app afterwards.
	 */
	fun setActiveAccount(id: String) {
		accountStore.setActive(id)
		api = createClient(accountStore.activeAccount)
		ttlLruCache.clear()
	}

	fun removeAccount(id: String) {
		val wasActive = accountStore.activeAccountId == id
		accountStore.remove(id)
		if (!wasActive) return

		api = createClient(accountStore.activeAccount)
		ttlLruCache.clear()

		val active = accountStore.activeAccount
		_state.value = if (active != null && active.hasCredentials) {
			SessionState.Restoring
		} else {
			SessionState.Unauthenticated
		}
	}

	fun refreshClient() {
		api = createClient(accountStore.activeAccount)
	}

	fun updateInstanceUrl(url: String) {
		val rawUrl = url.trim().removeSuffix("/")
		val formattedUrl = if (rawUrl.startsWith("https://") || rawUrl.startsWith("http://")) {
			rawUrl
		} else {
			"https://$rawUrl"
		}
		val account = accountStore.activeAccount ?: return
		if (formattedUrl.isNotBlank() && formattedUrl != account.baseUrl) {
			accountStore.updateBaseUrl(account.id, formattedUrl)
			refreshClient()
		}
	}

	fun getCoverArtUrl(coverArtId: String) = api.getCoverArtUrl(
		coverArtId,
		size = "${preferenceManager.coverArtQuality.value}"
	)

	suspend fun supportsApiKeyAuthentication(instanceUrl: String): Boolean =
		serverFactory.supportsApiKeyAuthentication(instanceUrl)
}

fun DomainUser.hasRole(role: String): Boolean {
	return this.roles.contains(role)
}

fun DomainUser.canShare(): Boolean {
	return this.hasRole("share")
}

fun SessionManager.canUserShare(): Boolean {
	return (sessionState.value as? SessionState.Authenticated)?.currentUser?.canShare() == true
}

fun SessionManager.getUser(): DomainUser? {
	return (sessionState.value as? SessionState.Authenticated)?.currentUser
}

fun SessionManager.getServerInfo(): ServerInfo? {
	return (sessionState.value as? SessionState.Authenticated)?.serverInfo
}

fun SessionManager.isNavidrome(): Boolean {
	return this.getServerInfo()?.type.equals("navidrome", ignoreCase = true)
}

fun SessionManager.isJellyfin(): Boolean {
	return this.getServerInfo()?.type.equals("jellyfin", ignoreCase = true)
}

fun SessionManager.supportsExtension(extension: ServerExtension): Boolean {
	return this.getServerInfo()?.extensions?.contains(extension) == true
}
