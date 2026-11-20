/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin

import android.content.Context
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.exception.InvalidContentException
import org.jellyfin.sdk.api.client.exception.InvalidStatusException
import org.jellyfin.sdk.api.operations.AuthenticationApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import paige.navic.BuildConfig
import paige.navic.domain.server.AuthorizationData
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.MusicServerFactory
import paige.navic.domain.server.UserCredentials
import paige.navic.domain.server.ServerException
import java.util.UUID

class JellyfinMusicServerFactory(
	private val context: Context
) : MusicServerFactory {

	private val jellyfin by lazy {
		createJellyfin {
			context = this@JellyfinMusicServerFactory.context
			clientInfo = ClientInfo(name = "Navic", version = BuildConfig.VERSION_NAME)
		}
	}

	override fun create(baseUrl: String, authorization: AuthorizationData): MusicServer {
		val jellyfinToken = authorization as? AuthorizationData.JellyfinToken
		val accessToken = jellyfinToken?.accessToken
		val userIdString =
			jellyfinToken?.userId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

		val apiClient = jellyfin.createApi(
			baseUrl = baseUrl,
			accessToken = accessToken
		)

		return JellyfinMusicServer(
			api = apiClient,
			baseUrl = baseUrl,
			userId = UUID.fromString(userIdString),
			accessToken = accessToken ?: ""
		)
	}

	override suspend fun authenticate(
		baseUrl: String,
		credentials: UserCredentials
	): AuthorizationData {
		val jellyfinCredentials = credentials as? UserCredentials.Jellyfin
			?: throw IllegalArgumentException("Jellyfin factory requires Jellyfin credentials")
		val apiClient = jellyfin.createApi(baseUrl = baseUrl)
		val authApi = AuthenticationApi(apiClient)

		val authResult = try {
			authApi.authenticateUserByName(
				data = org.jellyfin.sdk.model.api.AuthenticateUserByName(
					username = jellyfinCredentials.username,
					pw = jellyfinCredentials.password
				)
			).content
		} catch (e: InvalidStatusException) {
			if (e.status == 401 || e.status == 400 || e.status == 403) {
				throw InvalidCredentialsException("Invalid Jellyfin username or password", e)
			}
			throw ServerException("Jellyfin server returned HTTP ${e.status}", e)
		} catch (e: InvalidContentException) {
			throw InvalidCredentialsException("Invalid response from server or wrong credentials", e)
		} catch (e: ApiClientException) {
			throw ServerException("Failed to authenticate with Jellyfin server", e)
		} catch (e: Exception) {
			if (e is InvalidCredentialsException || e is ServerException) throw e
			throw ServerException("Failed to authenticate with Jellyfin server", e)
		}

		val token = authResult.accessToken ?: throw InvalidCredentialsException("No access token returned from Jellyfin")
		val userId = authResult.user?.id ?: throw InvalidCredentialsException("No user ID returned from Jellyfin")

		return AuthorizationData.JellyfinToken(
			accessToken = token,
			userId = userId.toString()
		)
	}
}
