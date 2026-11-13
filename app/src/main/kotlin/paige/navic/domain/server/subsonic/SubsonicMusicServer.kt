/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic

import dev.zt64.subsonic.api.model.MediaType
import dev.zt64.subsonic.api.model.PlaybackState
import dev.zt64.subsonic.api.model.SubsonicErrorCode
import dev.zt64.subsonic.api.model.SubsonicException
import dev.zt64.subsonic.api.model.SubsonicResponse
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistInfo
import paige.navic.domain.model.DomainChatMessage
import paige.navic.domain.model.DomainGenre
import paige.navic.domain.model.DomainPlaybackState
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainRadio
import paige.navic.domain.model.DomainSearchResult
import paige.navic.domain.model.DomainShare
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainUser
import paige.navic.domain.model.ServerLyrics
import paige.navic.domain.server.ChatServer
import paige.navic.domain.server.InvalidCredentialsException
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.RadioServer
import paige.navic.domain.server.ServerException
import paige.navic.domain.server.ServerExtension
import paige.navic.domain.server.ServerInfo
import paige.navic.domain.server.ServerStatus
import paige.navic.domain.server.ShareServer
import kotlin.time.Clock
import kotlin.time.Instant

internal class SubsonicMusicServer(
	private val client: SubsonicClient,
	private val username: String?
) : MusicServer, ChatServer, ShareServer, RadioServer {
	private companion object {
		val SEMVER_REGEX = Regex("""^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)""")
		const val ID3_SUPPORT = "v1.8.0"
	}

	private var extensions: Set<ServerExtension>? = null

	private data class SemanticVersioning(
		val major: Int = 0,
		val minor: Int = 0,
		val patch: Int = 0
	) : Comparable<SemanticVersioning> {
		override fun compareTo(other: SemanticVersioning): Int {
			if (this.major != other.major) return this.major.compareTo(other.major)
			if (this.minor != other.minor) return this.minor.compareTo(other.minor)
			return this.patch.compareTo(other.patch)
		}
	}

	private fun parseSubsonicVersion(version: String): SemanticVersioning? {
		val cleaned = version.trim().removePrefix("v")
		val matchResult = SEMVER_REGEX.find(cleaned) ?: return null

		return SemanticVersioning(
			major = matchResult.groupValues.getOrNull(1)?.toIntOrNull() ?: 0,
			minor = matchResult.groupValues.getOrNull(2)?.toIntOrNull() ?: 0,
			patch = matchResult.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
		)
	}

	private suspend fun isEqualOrAboveSubsonicVersion(target: String): Boolean {
		val currentVersion = serverInfo().version?.let {
			parseSubsonicVersion(it)
		} ?: return false

		val targetVersion = parseSubsonicVersion(target) ?: return false

		return currentVersion >= targetVersion
	}

	private suspend fun supportsID3() = isEqualOrAboveSubsonicVersion(ID3_SUPPORT)

	override suspend fun ping(): ServerStatus {
		val response = call { client.ping() }
		return ServerStatus(
			serverVersion = response.serverVersion,
			version = response.version,
			type = response.type,
			isOpenSubsonic = response.openSubsonic
		)
	}

	override suspend fun serverInfo(): ServerInfo {
		val response = call { client.ping() }
		val supportedExtensions = runCatching {
			client.getOpenSubsonicExtensions()
		}.getOrDefault(emptyList())

		val detected = if (response.openSubsonic) {
			buildSet {
				supportedExtensions.forEach { extension ->
					if (extension.name == "transcoding" && extension.versions.contains(1)) {
						add(ServerExtension.TRANSCODE_DECISION)
					}
				}

				if (runCatching { client.getChatMessages() }.isSuccess) {
					add(ServerExtension.CHAT)
				}
			}
		} else {
			emptySet()
		}

		extensions = detected

		return ServerInfo(
			extensions = detected,
			serverVersion = response.serverVersion,
			version = response.version,
			type = response.type,
			isOpenSubsonic = response.openSubsonic
		)
	}

	override suspend fun currentUser(): DomainUser {
		val tokenUsername = username ?: call { client.tokenInfo() }.username
		val user = if (username != null) {
			call { client.getUser(tokenUsername) }
		} else {
			runCatching { call { client.getUser(tokenUsername) } }.getOrNull()
		}

		return user?.toDomainModel() ?: DomainUser(username = tokenUsername)
	}

	override suspend fun getAlbums(
		type: DomainAlbumListType,
		size: Int,
		offset: Int
	): List<DomainAlbum> = call {
		if (supportsID3()) {
			client.getAlbumsID3(type.toApi(), size, offset)
		} else {
			client.getAlbums(type.toApi(), size, offset)
		}
	}.map { it.toDomainModel() }

	override suspend fun getAlbum(id: String): DomainAlbum =
		call { client.getAlbum(id) }.toDomainModel(includeSongs = true)

	override suspend fun getSong(id: String): DomainSong =
		call { client.getSong(id) }.toDomainModel()

	override suspend fun getArtists(): List<DomainArtist> =
		call { client.getArtists() }
			.index
			.flatMap { index -> index.artists }
			.map { it.toDomainModel() }

	override suspend fun getArtistInfo(id: String): DomainArtistInfo =
		call { client.getArtistInfo(id) }.toDomainModel()

	override suspend fun getAlbumInfo(id: String): DomainAlbumInfo = call {
		if (supportsID3()) {
			client.getAlbumInfoID3(id)
		} else {
			client.getAlbumInfo(id)
		}
	}.toDomainModel()

	override suspend fun getGenres(): List<DomainGenre> =
		call { client.getGenres() }.map { it.toDomainModel() }

	override suspend fun getPlaylists(): List<DomainPlaylist> =
		call { client.getPlaylists() }.map { it.toDomainModel() }

	override suspend fun getPlaylist(id: String): DomainPlaylist =
		call { client.getPlaylist(id) }.toDomainModel(includeSongs = true)

	override suspend fun getSimilarSongs(id: String, count: Int): List<DomainSong> =
		call {
			if (supportsID3()) {
				client.getSimilarSongsID3(id, count)
			} else {
				client.getSimilarSongs(id, count)
			}
		}.map { it.toDomainModel() }

	override suspend fun search(query: String): DomainSearchResult =
		call {
			if (supportsID3()) {
				client.searchID3(query)
			} else {
				client.search(query)
			}
		}.toDomainModel()

	override suspend fun getLyrics(songId: String): ServerLyrics? =
		call { client.getLyrics(songId) }.firstOrNull()?.toDomainModel()

	override suspend fun star(id: String) {
		call { client.star(id) }
	}

	override suspend fun unstar(id: String) {
		call { client.unstar(id) }
	}

	override suspend fun setRating(id: String, rating: Int) {
		call { client.setRating(id, rating) }
	}

	override suspend fun scrobble(id: String, submission: Boolean, time: Instant?) {
		call {
			client.scrobble(
				id = id,
				time = time ?: Clock.System.now(),
				submission = submission
			)
		}
	}

	override suspend fun reportPlayback(
		id: String,
		positionMs: Long,
		state: DomainPlaybackState,
		playbackRate: Float
	) {
		val apiState = when (state) {
			DomainPlaybackState.STARTING -> PlaybackState.STARTING
			DomainPlaybackState.PLAYING -> PlaybackState.PLAYING
			DomainPlaybackState.PAUSED -> PlaybackState.PAUSED
			DomainPlaybackState.STOPPED -> PlaybackState.STOPPED
		}
		call {
			client.reportPlayback(
				mediaId = id,
				mediaType = MediaType.SONG,
				positionMs = positionMs,
				state = apiState,
				playbackRate = playbackRate
			)
		}
	}

	override suspend fun createPlaylist(name: String, songIds: List<String>): DomainPlaylist =
		call { client.createPlaylist(name, songIds) }.toDomainModel()

	override suspend fun updatePlaylist(
		id: String,
		name: String?,
		comment: String?,
		public: Boolean?,
		songIdsToAdd: List<String>,
		songIndicesToRemove: List<Int>
	) {
		call {
			client.updatePlaylist(
				id = id,
				name = name,
				comment = comment,
				public = public,
				songIdsToAdd = songIdsToAdd,
				songIndicesToRemove = songIndicesToRemove
			)
		}
	}

	override suspend fun deletePlaylist(id: String) {
		call { client.deletePlaylist(id) }
	}

	override fun getStreamUrl(
		id: String,
		maxBitRate: Int,
		format: String?,
		estimateContentLength: Boolean
	): String = client.getStreamUrl(
		id = id,
		maxBitRate = maxBitRate,
		format = format,
		estimateContentLength = estimateContentLength
	)

	override suspend fun getPlaybackStreamUrl(
		id: String,
		bitrateKbps: Int,
		format: String?
	): String {
		val directUrl = getStreamUrl(
			id,
			maxBitRate = bitrateKbps,
			format
		)
		val supportedExtensions = extensions ?: serverInfo().extensions
		if (ServerExtension.TRANSCODE_DECISION !in supportedExtensions) return directUrl

		val decision = call {
			client.getTranscodeDecision(
				id,
				MediaType.SONG,
				SubsonicClientInfo.buildClientInfo(bitrateKbps, format)
			)
		}
		val shouldTranscode = !decision.canDirectPlay &&
			decision.canTranscode &&
			decision.transcodeParams != null
		if (shouldTranscode) {
			call {
				client.getTranscodeStreamUrl(
					id,
					MediaType.SONG,
					0,
					decision.transcodeParams!!
				)
			}
		}
		return directUrl
	}

	override fun getCoverArtUrl(id: String, size: String?): String =
		client.getCoverArtUrl(id, size = size, auth = true)

	override suspend fun getCoverArt(id: String, size: String?): ByteArray =
		call { client.getCoverArt(id, size = size) }

	override fun getAvatarUrl(username: String): String =
		client.getAvatarUrl(username, auth = true)

	override fun parseResponseError(contentType: String?, body: String): ServerException? {
		if (contentType != ContentType.Application.Json.contentType) return null

		return try {
			val response = Json.decodeFromString<SubsonicResponse<Any>>(body)
			if (response is SubsonicResponse.Error) {
				response.error.code.toServerException(response.error.message)
			} else {
				null
			}
		} catch (_: Exception) {
			null
		}
	}

	override suspend fun getChatMessages(): List<DomainChatMessage> =
		call { client.getChatMessages() }.map { it.toDomainModel() }

	override suspend fun addChatMessage(message: String) {
		call { client.addChatMessage(message) }
	}

	override suspend fun getShares(): List<DomainShare> =
		call { client.getShares() }.map { it.toDomainModel() }

	override suspend fun createShare(
		ids: List<String>,
		description: String?,
		expiresAt: Instant?,
		downloadable: Boolean
	): DomainShare = call {
		client.createShare(ids, description = description, expiresAt = expiresAt)
	}.toDomainModel()

	override suspend fun deleteShare(id: String) {
		call { client.deleteShare(id) }
	}

	override suspend fun getRadioStations(): List<DomainRadio> =
		call { client.getInternetRadioStations() }.map { it.toDomainModel() }

	override suspend fun createRadioStation(
		name: String,
		streamUrl: String,
		homepageUrl: String?
	) {
		call {
			client.createInternetRadioStation(
				name = name,
				streamUrl = streamUrl,
				homepageUrl = homepageUrl
			)
		}
	}

	override suspend fun updateRadioStation(
		id: String,
		name: String,
		streamUrl: String,
		homepageUrl: String?
	) {
		call {
			client.updateInternetRadioStation(
				id = id,
				name = name,
				streamUrl = streamUrl,
				homepageUrl = homepageUrl
			)
		}
	}

	override suspend fun deleteRadioStation(id: String) {
		call { client.deleteInternetRadioStation(id) }
	}

	private suspend fun <T> call(block: suspend () -> T): T = try {
		block()
	} catch (e: SubsonicException) {
		throw e.toServerException()
	}

	private fun SubsonicException.toServerException(): ServerException =
		code.toServerException(message ?: code.description)

	private fun SubsonicErrorCode.toServerException(message: String): ServerException =
		if (this == SubsonicErrorCode.WRONG_USERNAME_OR_PASSWORD ||
			this == SubsonicErrorCode.INVALID_API_KEY
		) {
			InvalidCredentialsException(message)
		} else {
			ServerException(message)
		}
}
