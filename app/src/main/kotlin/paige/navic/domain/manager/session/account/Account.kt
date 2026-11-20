/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session.account

import kotlinx.serialization.Serializable
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.AuthorizationData
import paige.navic.domain.server.ServerType
import paige.navic.domain.server.UserCredentials

@Serializable
data class Account(
	val id: String,
	val baseUrl: String,
	val serverType: ServerType? = null,
	val authMethod: AuthMethod = AuthMethod.Token,
	val username: String? = null,
	val password: String? = null,
	val apiKey: String? = null,
	val jellyfinToken: String? = null,
	val jellyfinUserId: String? = null
) {
	val hasCredentials: Boolean
		get() = credentials() != null || authorization() != null

	fun credentials(): UserCredentials? = when {
		serverType == ServerType.JELLYFIN -> {
			UserCredentials.Jellyfin(
				username ?: return null,
				password ?: return null
			)
		}

		authMethod == AuthMethod.ApiKey ->
			apiKey?.takeIf(String::isNotBlank)?.let(UserCredentials::ApiKey)

		else -> {
			UserCredentials.Password(
				username?.takeIf(String::isNotBlank) ?: return null,
				password?.takeIf(String::isNotBlank) ?: return null
			)
		}
	}

	fun authorization(): AuthorizationData? = when {
		serverType == ServerType.JELLYFIN -> {
			val token = jellyfinToken?.takeIf(String::isNotBlank) ?: return null
			val userId = jellyfinUserId?.takeIf(String::isNotBlank) ?: return null
			AuthorizationData.JellyfinToken(token, userId)
		}

		authMethod == AuthMethod.ApiKey -> apiKey?.takeIf(String::isNotBlank)
				?.let(AuthorizationData::SubsonicApiKey)

		else -> {
			val user = username?.takeIf(String::isNotBlank) ?: return null
			val pass = password?.takeIf(String::isNotBlank) ?: return null
			AuthorizationData.SubsonicToken(user, pass)
		}
	}
}
