/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.navidrome.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.ServerException
import paige.navic.domain.server.navidrome.api.domain.NavidromeAuthResponse
import paige.navic.domain.server.navidrome.api.domain.NavidromeCredentials
import kotlin.time.Instant

class NavidromeClient(
	baseUrl: String,
	private val username: String,
	private val password: String,
	engine: HttpClientEngine
) {
	private val baseUrl = baseUrl.removeSuffix("/")
	private val json = Json { ignoreUnknownKeys = true }

	private val client = HttpClient(engine) {
		install(ContentNegotiation) {
			json(json)
		}
	}

	@Volatile
	private var authToken: String? = null

	suspend fun getSongFilePath(id: String): String {
		val response = authorizedRequest {
			method = HttpMethod.Get
			url("$baseUrl/api/song/$id")
			accept(ContentType.Application.Json)
		}

		return json.decodeFromString<NavidromeSong>(
			response.bodyAsText()
		).absolutePath
	}

	suspend fun getAlbumSongPaths(albumId: String): Map<String, String> {
		val response = authorizedRequest {
			method = HttpMethod.Get
			url("$baseUrl/api/song")
			parameter("_filters", """{"album_id":"$albumId"}""")
			accept(ContentType.Application.Json)
		}

		return json.decodeFromString<List<NavidromeSong>>(
			response.bodyAsText()
		).associate { it.id to it.absolutePath }
	}

	suspend fun uploadPlaylistCover(id: String, bytes: ByteArray) {
		val part = PartData.BinaryChannelItem(
			provider = { ByteReadChannel(bytes) },
			partHeaders = Headers.build {
				append(
					HttpHeaders.ContentDisposition,
					"form-data; name=\"image\"; filename=\"cover.jpg\""
				)
			}
		)

		val response = authorizedRequest {
			method = HttpMethod.Post
			url("$baseUrl/api/playlist/$id/image")
			setBody(MultiPartFormDataContent(listOf(part)))
		}

		if (!response.status.isSuccess()) {
			throw ServerException("Failed to upload playlist cover (${response.status})")
		}
	}

	suspend fun removePlaylistCover(id: String) {
		val response = authorizedRequest {
			method = HttpMethod.Delete
			url("$baseUrl/api/playlist/$id/image")
		}

		if (!response.status.isSuccess()) {
			throw ServerException("Failed to remove playlist cover (${response.status})")
		}
	}

	suspend fun createShare(
		resourceIds: List<String>,
		description: String? = null,
		downloadable: Boolean = false,
		expiresAt: Instant? = null
	): String {
		val payload = CreateShareRequest(
			resourceIds = resourceIds.joinToString(","),
			description = description,
			downloadable = downloadable,
			expiresAt = expiresAt?.toString()
		)

		val response = authorizedRequest {
			method = HttpMethod.Post
			url("$baseUrl/api/share")
			contentType(ContentType.Application.Json)
			setBody(payload)
		}

		if (!response.status.isSuccess()) {
			throw ServerException("Failed to create Navidrome share (${response.status})")
		}

		return json.decodeFromString<NavidromeShareResponse>(response.bodyAsText()).id
	}

	private suspend fun authorizedRequest(
		builder: HttpRequestBuilder.() -> Unit
	): HttpResponse {
		suspend fun send(token: String) = client.request {
			header("x-nd-authorization", "Bearer $token")
			builder()
		}

		var response = send(authToken ?: login())
		if (response.status == HttpStatusCode.Unauthorized) {
			response = send(login())
		}

		return response
	}

	private suspend fun login(): String {
		val response = client.post("$baseUrl/auth/login") {
			contentType(ContentType.Application.Json)
			setBody(NavidromeCredentials(username = username, password = password))
		}

		if (!response.status.isSuccess()) {
			throw InvalidCredentialsException("Failed to log in to Navidrome (${response.status})")
		}

		return json.decodeFromString<NavidromeAuthResponse>(response.bodyAsText())
			.token
			.also { authToken = it }
	}
}

@Serializable
private data class CreateShareRequest(
	val resourceIds: String,
	val description: String?,
	val downloadable: Boolean,
	val expiresAt: String?
)

@Serializable
private data class NavidromeShareResponse(val id: String)

@Serializable
private data class NavidromeSong(
	val id: String,
	val path: String,
	val libraryPath: String
) {
	val absolutePath: String
		get() {
			val separator = if (libraryPath.contains('\\')) "\\" else "/"
			return libraryPath.trimEnd('/', '\\') + separator + path.trimStart('/', '\\')
		}
}
