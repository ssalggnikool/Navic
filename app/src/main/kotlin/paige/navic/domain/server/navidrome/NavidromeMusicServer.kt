/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.navidrome

import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainShare
import paige.navic.domain.model.DomainShareItem
import paige.navic.domain.model.DomainSong
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.PlaylistCoverServer
import paige.navic.domain.server.ServerException
import paige.navic.domain.server.ShareServer
import paige.navic.domain.server.navidrome.api.NavidromeClient
import kotlin.time.Clock
import kotlin.time.Instant

internal class NavidromeMusicServer(
	private val delegate: MusicServer,
	private val subsonicShares: ShareServer,
	private val navidrome: NavidromeClient,
	private val baseUrl: String,
	private val username: String
) : MusicServer by delegate, ShareServer, PlaylistCoverServer {
	@Volatile
	private var isNavidrome: Boolean? = null

	private suspend fun navidromeOrNull(): NavidromeClient? {
		val navidrome = isNavidrome ?: delegate.serverInfo()
			.type
			.equals("navidrome", ignoreCase = true)
			.also { isNavidrome = it }

		return this.navidrome.takeIf { navidrome }
	}

	override suspend fun getAlbum(id: String): DomainAlbum {
		val album = delegate.getAlbum(id)
		val paths = navidromeOrNull()?.getAlbumSongPaths(id).orEmpty()

		return album.copy(
			songs = album.songs.map { song ->
				paths[song.id]?.let { song.copy(filePath = it) } ?: song
			}
		)
	}

	override suspend fun getSong(id: String): DomainSong {
		val subsonicSong = delegate.getSong(id)
		val realFilePath = navidromeOrNull()?.getSongFilePath(id)
			?: return subsonicSong

		return subsonicSong.copy(
			filePath = realFilePath
		)
	}

	override suspend fun getShares(): List<DomainShare> = subsonicShares.getShares()

	override suspend fun createShare(
		ids: List<String>,
		description: String?,
		expiresAt: Instant?,
		downloadable: Boolean
	): DomainShare {
		val client = navidromeOrNull()
			?: return subsonicShares.createShare(ids, description, expiresAt, downloadable)

		val id = client.createShare(ids, description, downloadable, expiresAt)

		return DomainShare(
			id = id,
			url = "${baseUrl.removeSuffix("/")}/share/$id",
			description = description,
			username = username,
			createdAt = Clock.System.now(),
			expiresAt = expiresAt,
			lastVisited = null,
			visitCount = 0,
			items = ids.map { DomainShareItem(id = it, coverArtId = null) }
		)
	}

	override suspend fun deleteShare(id: String) = subsonicShares.deleteShare(id)

	override suspend fun uploadPlaylistCover(id: String, bytes: ByteArray) {
		val client = navidromeOrNull()
			?: throw ServerException(
				"Playlist cover upload only works for Navidrome servers for now. Sorry."
			)

		client.uploadPlaylistCover(id, bytes)
	}

	override suspend fun removePlaylistCover(id: String) {
		val client = navidromeOrNull()
			?: throw ServerException(
				"Playlist cover removal only works for Navidrome servers for now. Sorry."
			)

		client.removePlaylistCover(id)
	}
}
