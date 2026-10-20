/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import dev.zt64.subsonic.api.model.Role
import dev.zt64.subsonic.api.model.User
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.util.Logger
import paige.navic.util.getEncryptedString
import paige.navic.util.insertEncryptedString
import paige.navic.util.network.NavicularDnsResolver
import paige.navic.util.network.buildKtorEngine

class SessionManager(
	private val settings: Settings,
	private val preferenceManager: PreferenceManager,
	private val dnsResolver: NavicularDnsResolver
) {

	val isLoggedIn: StateFlow<Boolean>
		field = MutableStateFlow(false)

	private var currentUser: User? = null
	private val mutex = Mutex()
	private val scope = CoroutineScope(Dispatchers.IO)

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
		isLoggedIn.value = settings.getStringOrNull("username") != null
		if (isLoggedIn.value) getCachedUser()
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
		client = "Navic",
		clientConfig = {
			install(UserAgent) {
				agent = "Navic"
			}

			val customHeaders = preferenceManager.customHeadersMap()
			if (customHeaders.isNotEmpty()) {
				defaultRequest {
					customHeaders.forEach { (key, value) -> header(key, value) }
				}
			}
		},
		engine = buildKtorEngine(
			preferenceManager.proxyUrl,
			preferenceManager.dangerousSslNoopEnabled,
			dnsResolver
		)
	)

	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		val client = createClient(instanceUrl, username, password)

		try {
			client.ping()
			fetchCurrentUser(username, client)
		} catch (e: Exception) {
			// TODO: custom exception instead of the generic "Exception"
			throw Exception(
				"Failed to connect to the instance. Please check your credentials and try again.",
				e
			)
		}

		this.instanceUrl = instanceUrl
		this.username = username
		this.password = password

		api = client
		isLoggedIn.value = true
	}

	fun logout() {
		settings["username"] = null
		settings.remove("password")
		isLoggedIn.value = false
		currentUser = null
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


	private suspend fun fetchCurrentUser(
		username: String = this.username,
		client: SubsonicClient = api
	): User? {
		mutex.withLock {
			if (username.isNotBlank()) {
				currentUser = client.getUser(username)
				return currentUser
			}
		}

		// TODO: custom exception instead of the generic "Exception"
		throw Exception("Failed to get current user because the username is blank")
	}

	fun getCachedUser(): User? {
		if (currentUser != null) {
			return currentUser
		}
		scope.launch {
			try {
				fetchCurrentUser()
			} catch (e: Exception) {
				Logger.e("SessionManager", "Failed to fetch current user info", e)
			}
		}
		return currentUser
	}
}

fun User.hasRole(role: Role): Boolean {
	return this.roles.contains(role)
}

fun User.canShare(): Boolean {
	return this.hasRole(Role.SHARE)
}

fun SessionManager.canUserShare(): Boolean {
	return this.getCachedUser()?.canShare() ?: false
}
