/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistInfo
import paige.navic.domain.model.DomainChatMessage
import paige.navic.domain.model.DomainGenre
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainRadio
import paige.navic.domain.model.DomainSearchResult
import paige.navic.domain.model.DomainShare
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainUser
import paige.navic.domain.model.ServerLyrics
import kotlin.time.Instant

interface MusicServer {
	suspend fun ping(): ServerStatus

	suspend fun serverInfo(): ServerInfo

	suspend fun currentUser(): DomainUser

	suspend fun getAlbums(
		type: DomainAlbumListType,
		size: Int = 10,
		offset: Int = 0
	): List<DomainAlbum>

	suspend fun getAlbum(id: String): DomainAlbum

	suspend fun getSong(id: String): DomainSong

	suspend fun getArtists(): List<DomainArtist>

	suspend fun getArtistInfo(id: String): DomainArtistInfo

	suspend fun getAlbumInfo(id: String): DomainAlbumInfo

	suspend fun getGenres(): List<DomainGenre>

	suspend fun getPlaylists(): List<DomainPlaylist>

	suspend fun getPlaylist(id: String): DomainPlaylist

	suspend fun getSimilarSongs(id: String, count: Int = 50): List<DomainSong>

	suspend fun search(query: String): DomainSearchResult

	suspend fun getLyrics(songId: String): ServerLyrics?

	suspend fun star(id: String)

	suspend fun unstar(id: String)

	suspend fun setRating(id: String, rating: Int)

	suspend fun scrobble(
		id: String,
		submission: Boolean = true,
		time: Instant? = null
	)

	suspend fun createPlaylist(name: String, songIds: List<String>): DomainPlaylist

	suspend fun updatePlaylist(
		id: String,
		name: String? = null,
		comment: String? = null,
		public: Boolean? = null,
		songIdsToAdd: List<String> = emptyList(),
		songIndicesToRemove: List<Int> = emptyList()
	)

	suspend fun deletePlaylist(id: String)

	fun getStreamUrl(
		id: String,
		maxBitRate: Int = 0,
		format: String? = null,
		estimateContentLength: Boolean = false
	): String

	suspend fun getPlaybackStreamUrl(
		id: String,
		bitrateKbps: Int = 0,
		format: String? = null
	): String

	fun getCoverArtUrl(id: String, size: String? = null): String

	suspend fun getCoverArt(id: String, size: String? = null): ByteArray

	fun getAvatarUrl(username: String): String?

	fun parseResponseError(contentType: String?, body: String): ServerException?
}

interface ChatServer {
	suspend fun getChatMessages(): List<DomainChatMessage>

	suspend fun addChatMessage(message: String)
}

interface ShareServer {
	suspend fun getShares(): List<DomainShare>

	suspend fun createShare(
		ids: List<String>,
		description: String? = null,
		expiresAt: Instant? = null
	): DomainShare

	suspend fun deleteShare(id: String)
}

interface RadioServer {
	suspend fun getRadioStations(): List<DomainRadio>

	suspend fun createRadioStation(name: String, streamUrl: String, homepageUrl: String? = null)

	suspend fun updateRadioStation(
		id: String,
		name: String,
		streamUrl: String,
		homepageUrl: String? = null
	)

	suspend fun deleteRadioStation(id: String)
}
