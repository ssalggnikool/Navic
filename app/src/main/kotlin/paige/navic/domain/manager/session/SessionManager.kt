/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.DomainUser
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.ChatServer
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.MusicServerFactory
import paige.navic.domain.server.RadioServer
import paige.navic.domain.server.ServerCredentials
import paige.navic.domain.server.ServerExtension
import paige.navic.domain.server.ServerInfo
import paige.navic.domain.server.ShareServer
import paige.navic.util.Logger
import paige.navic.util.TTLLruCache
import paige.navic.util.getEncryptedString
import paige.navic.util.insertEncryptedString
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
	private val settings: Settings,
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

	private val hasStoredCredentials: Boolean
		get() = instanceUrl.isNotBlank() && when (authMethod) {
			AuthMethod.Token -> username.isNotBlank() && password.isNotBlank()
			AuthMethod.ApiKey -> apiKey.isNotBlank()
		}

	private val _state = MutableStateFlow(
		if (hasStoredCredentials) SessionState.Restoring else SessionState.Unauthenticated
	)
	val sessionState: StateFlow<SessionState> = _state.asStateFlow()

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	var username: String
		get() = settings.getString("username", "")
		private set(value) {
			settings["username"] = value
		}

	var password: String
		get() = settings.getEncryptedString("password", "")
		private set(value) {
			settings.insertEncryptedString("password", value)
		}

	var instanceUrl: String
		get() = settings.getString("instanceUrl", "")
		private set(value) {
			settings["instanceUrl"] = value
		}

	var authMethod: AuthMethod
		get() = runCatching {
			AuthMethod.valueOf(settings.getString("authMethod", AuthMethod.Token.name))
		}.getOrDefault(AuthMethod.Token)
		private set(value) {
			settings["authMethod"] = value.name
		}

	var apiKey: String
		get() = settings.getEncryptedString("apiKey", "")
		private set(value) {
			settings.insertEncryptedString("apiKey", value)
		}

	private fun storedCredentials(): ServerCredentials = when (authMethod) {
		AuthMethod.Token -> ServerCredentials.Token(username = username, password = password)
		AuthMethod.ApiKey -> ServerCredentials.ApiKey(apiKey = apiKey)
	}

	var api: MusicServer = serverFactory.create(
		baseUrl = instanceUrl,
		credentials = storedCredentials()
	)
		private set

	val chat: ChatServer?
		get() = api as? ChatServer

	val shares: ShareServer?
		get() = api as? ShareServer

	val radio: RadioServer?
		get() = api as? RadioServer

	init {
		if (hasStoredCredentials) {
			scope.launch {
				try {
					login(instanceUrl, storedCredentials())
				} catch (e: InvalidCredentialsException) {
					Logger.e(TAG, "Credentials invalid, logging out", e)
					logout()
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

	@Throws(InvalidCredentialsException::class)
	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) = login(
		instanceUrl = instanceUrl,
		credentials = ServerCredentials.Token(username = username, password = password)
	)

	@Throws(InvalidCredentialsException::class)
	suspend fun login(
		instanceUrl: String,
		apiKey: String
	) = login(
		instanceUrl = instanceUrl,
		credentials = ServerCredentials.ApiKey(apiKey = apiKey)
	)

	private suspend fun login(instanceUrl: String, credentials: ServerCredentials) {
		val server = serverFactory.create(
			baseUrl = instanceUrl,
			credentials = credentials
		)

		val serverInfo = server.serverInfo()
		Logger.i(TAG, "Ping result: $serverInfo")

		val user = server.currentUser()

		this.instanceUrl = instanceUrl
		when (credentials) {
			is ServerCredentials.Token -> {
				authMethod = AuthMethod.Token
				username = credentials.username
				password = credentials.password
				settings.remove("apiKey")
			}

			is ServerCredentials.ApiKey -> {
				authMethod = AuthMethod.ApiKey
				username = user.username
				settings.remove("password")
				apiKey = credentials.apiKey
			}
		}

		api = server
		_state.value = SessionState.Authenticated(
			currentUser = user,
			serverInfo = serverInfo
		)
	}

	fun logout() {
		settings["username"] = null
		settings.remove("password")
		settings.remove("apiKey")
		ttlLruCache.clear()
		_state.value = SessionState.Unauthenticated
	}

	fun refreshClient() {
		api = serverFactory.create(
			baseUrl = instanceUrl,
			credentials = storedCredentials()
		)
	}

	fun updateInstanceUrl(url: String) {
		val rawUrl = url.trim().removeSuffix("/")
		val formattedUrl = if (rawUrl.startsWith("https://") || rawUrl.startsWith("http://")) {
			rawUrl
		} else {
			"https://$rawUrl"
		}
		if (formattedUrl.isNotBlank() && formattedUrl != instanceUrl) {
			instanceUrl = formattedUrl
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

fun SessionManager.supportsExtension(extension: ServerExtension): Boolean {
	return this.getServerInfo()?.extensions?.contains(extension) == true
}
