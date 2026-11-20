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
	override fun create(baseUrl: String, authorization: AuthorizationData): MusicServer {
		return when (authorization) {
			is AuthorizationData.JellyfinToken -> jellyfinFactory.create(baseUrl, authorization)
			else -> navidromeFactory.create(baseUrl, authorization)
		}
	}

	override suspend fun authenticate(
		baseUrl: String,
		credentials: UserCredentials
	): AuthorizationData = when (credentials) {
		is UserCredentials.Jellyfin -> jellyfinFactory.authenticate(baseUrl, credentials)
		else -> subsonicFactory.authenticate(baseUrl, credentials)
	}
}
