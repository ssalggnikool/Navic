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
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.MusicServerFactory
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.buildKtorEngine
import paige.navic.util.network.createOkHttpClient
import paige.navic.util.network.getProxyUrlOrNull

class SubsonicMusicServerFactory(
	private val preferenceManager: PreferenceManager,
	private val dnsResolver: NavicDnsResolver
) : MusicServerFactory {
	override fun create(baseUrl: String, username: String, password: String): MusicServer {
		val client = SubsonicClient.Companion(
			baseUrl = baseUrl,
			auth = SubsonicAuth.Token(username = username, password = password),
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

		return SubsonicMusicServer(client, username)
	}

	private companion object {
		const val USER_AGENT = "Navic"
	}
}
