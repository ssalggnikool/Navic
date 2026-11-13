/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.navidrome

import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.ServerCredentials
import paige.navic.domain.server.ShareServer
import paige.navic.domain.server.navidrome.api.NavidromeClient
import paige.navic.domain.server.subsonic.SubsonicMusicServerFactory
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.buildKtorEngine
import paige.navic.util.network.createOkHttpClient
import paige.navic.util.network.getProxyUrlOrNull

class NavidromeMusicServerFactory(
	private val preferenceManager: PreferenceManager,
	private val dnsResolver: NavicDnsResolver
) : SubsonicMusicServerFactory(
	preferenceManager, dnsResolver
) {
	override fun create(baseUrl: String, credentials: ServerCredentials): MusicServer {
		val server = super.create(baseUrl, credentials)
		val token = credentials as? ServerCredentials.Token ?: return server
		val shares = server as? ShareServer ?: return server

		val engine = buildKtorEngine(
			createOkHttpClient(
				proxyUrl = preferenceManager.proxyUrl.getProxyUrlOrNull(),
				trustAllCerts = preferenceManager.dangerousSslNoopEnabled,
				dnsResolver
			)
		)

		return NavidromeMusicServer(
			delegate = server,
			subsonicShares = shares,
			navidrome = NavidromeClient(
				baseUrl = baseUrl,
				username = token.username,
				password = token.password,
				engine = engine
			),
			baseUrl = baseUrl,
			username = token.username
		)
	}
}
