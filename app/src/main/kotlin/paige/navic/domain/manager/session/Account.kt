/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session

import kotlinx.serialization.Serializable
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.ServerCredentials

@Serializable
data class Account(
	val id: String,
	val baseUrl: String,
	val authMethod: AuthMethod = AuthMethod.Token,
	val username: String = "",
	val password: String = "",
	val apiKey: String = "",
	val jellyfinToken: String = "",
	val jellyfinUserId: String = ""
) {
	val hasCredentials: Boolean
		get() = when (authMethod) {
			AuthMethod.Token -> username.isNotBlank() && password.isNotBlank()
			AuthMethod.ApiKey -> apiKey.isNotBlank()
			AuthMethod.Jellyfin -> jellyfinToken.isNotBlank() && jellyfinUserId.isNotBlank()
		}

	fun credentials(): ServerCredentials = when (authMethod) {
		AuthMethod.Token -> ServerCredentials.Token(username, password)
		AuthMethod.ApiKey -> ServerCredentials.ApiKey(apiKey)
		AuthMethod.Jellyfin -> ServerCredentials.Jellyfin(
			username = username,
			accessToken = jellyfinToken,
			userId = jellyfinUserId
		)
	}
}
