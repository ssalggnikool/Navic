/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

enum class AuthMethod {
	Token,
	ApiKey
}

sealed interface AuthorizationData {
	data class SubsonicToken(
		val username: String,
		val password: String
	) : AuthorizationData

	data class SubsonicApiKey(
		val apiKey: String
	) : AuthorizationData

	data class JellyfinToken(
		val accessToken: String,
		val userId: String
	) : AuthorizationData
}

sealed interface UserCredentials {
	data class Password(val username: String, val password: String) : UserCredentials

	data class ApiKey(val apiKey: String) : UserCredentials

	data class Jellyfin(val username: String, val password: String) : UserCredentials
}
