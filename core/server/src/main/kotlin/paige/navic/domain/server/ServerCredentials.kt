/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

enum class AuthMethod {
	Token,
	ApiKey,
	Jellyfin
}

sealed interface ServerCredentials {
	data class Token(val username: String, val password: String) : ServerCredentials

	data class ApiKey(val apiKey: String) : ServerCredentials

	data class Jellyfin(
		val username: String,
		val accessToken: String,
		val userId: String
	) : ServerCredentials
}
