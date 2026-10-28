/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import dev.zt64.subsonic.api.model.Role
import dev.zt64.subsonic.api.model.SubsonicErrorCode
import dev.zt64.subsonic.api.model.SubsonicException
import dev.zt64.subsonic.api.model.User
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.util.Logger
import paige.navic.util.TTLLruCache
import paige.navic.util.getEncryptedString
import paige.navic.util.insertEncryptedString
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.buildKtorEngine
import paige.navic.util.network.createOkHttpClient
import paige.navic.util.network.getProxyUrlOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

sealed interface SessionState {
	data object Unauthenticated : SessionState
	data object Restoring : SessionState
	data class Authenticated(
		val currentUser: User?,
		/* use this to check the music server being used */
		val serverInfo: Any? = null
	) : SessionState
}

class SessionManager(
	private val settings: Settings,
	private val preferenceManager: PreferenceManager,
	private val dnsResolver: NavicDnsResolver
) {
	val ttlLruCache = TTLLruCache<String, String>(
		maxSize = 500,
		defaultTtlMillis = 30.minutes.toLong(DurationUnit.MILLISECONDS)
	)

	private companion object {
		const val TAG = "SessionManager"
		const val USER_AGENT = "Navic"
	}

	private val hasStoredCredentials: Boolean
		get() = username.isNotBlank() && password.isNotBlank() && instanceUrl.isNotBlank()

	private val _state = MutableStateFlow(
		if (hasStoredCredentials) SessionState.Restoring else SessionState.Unauthenticated
	)
	val sessionState: StateFlow<SessionState> = _state.asStateFlow()

	private val _supportsTranscoding = MutableStateFlow(
		settings.getBoolean("supportsTranscoding", false)
	)
	val supportsTranscoding: StateFlow<Boolean> = _supportsTranscoding.asStateFlow()

	private val mutex = Mutex()
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

	var api: SubsonicClient = createClient(
		instanceUrl = instanceUrl,
		username = username,
		password = password
	)
		private set

	init {
		if (hasStoredCredentials) {
			scope.launch {
				try {
					login(instanceUrl, username, password)
				} catch (e: SubsonicException) {
					if (e.code == SubsonicErrorCode.WRONG_USERNAME_OR_PASSWORD) {
						Logger.e(TAG, "Credentials invalid, logging out", e)
						logout()
					} else {
						Logger.w(TAG, "SubsonicException during restoration: $e")
						_state.value = SessionState.Authenticated(currentUser = null, serverInfo = null)
					}
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

	private fun createClient(
		instanceUrl: String,
		username: String,
		password: String
	) = SubsonicClient.Companion(
		baseUrl = instanceUrl,
		auth = SubsonicAuth.Token(
			username = username,
			password = password,
		),
		client = USER_AGENT,
		clientConfig = {
			install(UserAgent) {
				agent = USER_AGENT
			}

			val customHeaders = preferenceManager.customHeadersMap()
			if (customHeaders.isNotEmpty()) {
				defaultRequest {
					customHeaders.forEach { (key, value) -> header(key, value) }
				}
			}
		},
		engine = buildKtorEngine(
			createOkHttpClient(
				proxyUrl = preferenceManager.proxyUrl.getProxyUrlOrNull(),
				trustAllCerts = preferenceManager.dangerousSslNoopEnabled,
				dnsResolver
			)
		)
	)

	@Throws(SubsonicException::class)
	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		val client = createClient(instanceUrl, username, password)

		val pingResult = client.ping()
		Logger.i(TAG, "Ping result: $pingResult")

		val user = fetchCurrentUser(username, client)

		val extensions = runCatching {
			client.getOpenSubsonicExtensions()
		}.getOrDefault(emptyList())
		_supportsTranscoding.value = extensions.any { it.name == "transcoding" }
		settings["supportsTranscoding"] = _supportsTranscoding.value

		this.instanceUrl = instanceUrl
		this.username = username
		this.password = password

		api = client
		_state.value = SessionState.Authenticated(
			currentUser = user,
			serverInfo = pingResult
		)
	}

	fun logout() {
		settings["username"] = null
		settings.remove("password")
		_supportsTranscoding.value = false
		settings["supportsTranscoding"] = false
		ttlLruCache.clear()
		_state.value = SessionState.Unauthenticated
	}

	fun refreshClient() {
		api = createClient(
			instanceUrl = instanceUrl,
			username = username,
			password = password,
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
		auth = true,
		size = "${preferenceManager.coverArtQuality.value}"
	)

	// TODO: use getTokenInfo instead to retrieve the username, will be required when implementing API key support
	private suspend fun fetchCurrentUser(
		username: String = this.username,
		client: SubsonicClient = api
	): User = mutex.withLock {
		// TODO: custom exception instead of the generic "Exception"
		if (username.isBlank()) {
			throw Exception("Failed to get current user because the username is blank")
		}

		client.getUser(username)
	}
}

fun User.hasRole(role: Role): Boolean {
	return this.roles.contains(role)
}

fun User.canShare(): Boolean {
	return this.hasRole(Role.SHARE)
}

fun SessionManager.canUserShare(): Boolean {
	return (sessionState.value as? SessionState.Authenticated)?.currentUser?.canShare() == true
}
