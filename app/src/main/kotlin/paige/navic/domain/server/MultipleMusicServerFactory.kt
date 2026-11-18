/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

import paige.navic.domain.server.jellyfin.JellyfinMusicServerFactory
import paige.navic.domain.server.navidrome.NavidromeMusicServerFactory
import paige.navic.domain.server.subsonic.SubsonicMusicServerFactory

class MultipleMusicServerFactory(
	private val navidromeFactory: NavidromeMusicServerFactory,
	private val subsonicFactory: SubsonicMusicServerFactory,
	val jellyfinFactory: JellyfinMusicServerFactory
) : MusicServerFactory {

	override fun create(baseUrl: String, credentials: ServerCredentials): MusicServer {
		return when (credentials) {
			is ServerCredentials.Jellyfin -> jellyfinFactory.create(baseUrl, credentials)
			else -> navidromeFactory.create(baseUrl, credentials)
		}
	}

	override suspend fun supportsApiKeyAuthentication(baseUrl: String): Boolean {
		return subsonicFactory.supportsApiKeyAuthentication(baseUrl)
	}
}
