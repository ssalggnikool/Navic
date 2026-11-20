/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic

import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import paige.navic.preferences.AccountSettings
import paige.navic.preferences.PreferenceManager
import paige.navic.domain.server.AuthorizationData
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.MusicServerFactory
import paige.navic.domain.server.UserCredentials
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.buildKtorEngine
import paige.navic.util.network.createOkHttpClient
import paige.navic.util.network.getProxyUrlOrNull

open class SubsonicMusicServerFactory(
	private val preferenceManager: PreferenceManager,
	private val accountSettings: AccountSettings,
	private val dnsResolver: NavicDnsResolver
) : MusicServerFactory {
	override fun create(baseUrl: String, authorization: AuthorizationData): MusicServer {
		val auth = when (authorization) {
			is AuthorizationData.SubsonicToken -> SubsonicAuth.Token(
				username = authorization.username,
				password = authorization.password
			)
			is AuthorizationData.SubsonicApiKey -> SubsonicAuth.Key(apiKey = authorization.apiKey)
			else -> SubsonicAuth.None
		}

		return SubsonicMusicServer(
			client = createClient(baseUrl, auth),
			username = (authorization as? AuthorizationData.SubsonicToken)?.username
		)
	}

	override suspend fun authenticate(
		baseUrl: String,
		credentials: UserCredentials
	): AuthorizationData = when (credentials) {
		is UserCredentials.Password -> AuthorizationData.SubsonicToken(
			username = credentials.username,
			password = credentials.password
		)
		is UserCredentials.ApiKey -> AuthorizationData.SubsonicApiKey(credentials.apiKey)
		is UserCredentials.Jellyfin -> throw IllegalArgumentException("Jellyfin credentials require the Jellyfin factory")
	}

	private fun createClient(baseUrl: String, auth: SubsonicAuth) = SubsonicClient.Companion(
		baseUrl = baseUrl,
		auth = auth,
		client = USER_AGENT,
		clientConfig = {
			install(UserAgent) {
				agent = USER_AGENT
			}

			val customHeaders = accountSettings.customHeadersMap()
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

	private companion object {
		const val USER_AGENT = "Navic"
	}
}
