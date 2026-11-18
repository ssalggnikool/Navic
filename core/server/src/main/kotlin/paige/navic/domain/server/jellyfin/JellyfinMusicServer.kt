/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin

import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.operations.InstantMixApi
import org.jellyfin.sdk.api.operations.LibraryApi
import org.jellyfin.sdk.api.operations.PlaylistApi
import org.jellyfin.sdk.api.operations.SearchApi
import org.jellyfin.sdk.api.operations.SessionApi
import org.jellyfin.sdk.api.operations.SystemApi
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.api.operations.UserDataApi
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.PlaybackOrder
import org.jellyfin.sdk.model.api.PlaybackProgressInfo
import org.jellyfin.sdk.model.api.PlaybackStartInfo
import org.jellyfin.sdk.model.api.PlaybackStopInfo
import org.jellyfin.sdk.model.api.RepeatMode
import org.jellyfin.sdk.model.api.SortOrder
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistInfo
import paige.navic.domain.model.DomainGenre
import paige.navic.domain.model.DomainPlaybackState
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSearchResult
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainUser
import paige.navic.domain.model.ServerLyrics
import paige.navic.domain.server.jellyfin.mapper.toDomainAlbum
import paige.navic.domain.server.jellyfin.mapper.toDomainArtist
import paige.navic.domain.server.jellyfin.mapper.toDomainPlaylist
import paige.navic.domain.server.jellyfin.mapper.toDomainSong
import paige.navic.domain.server.MusicServer
import paige.navic.domain.server.ServerException
import paige.navic.domain.server.ServerInfo
import paige.navic.domain.server.ServerStatus
import java.util.UUID
import kotlin.time.Instant

class JellyfinMusicServer(
	api: ApiClient,
	private val baseUrl: String,
	private val userId: UUID,
	private val accessToken: String
) : MusicServer {

	private val systemApi = SystemApi(api)
	private val userApi = UserApi(api)
	private val libraryApi = LibraryApi(api)
	private val userDataApi = UserDataApi(api)
	private val instantMixApi = InstantMixApi(api)
	private val playlistApi = PlaylistApi(api)
	private val sessionApi = SessionApi(api)
	private val searchApi = SearchApi(api)

	override suspend fun ping(): ServerStatus {
		val info = systemApi.getPublicSystemInfo().content
		return ServerStatus(
			serverVersion = info.version,
			version = info.version,
			type = "jellyfin",
			isOpenSubsonic = false
		)
	}

	override suspend fun serverInfo(): ServerInfo {
		val info = systemApi.getPublicSystemInfo().content
		return ServerInfo(
			serverVersion = info.version,
			version = info.version,
			type = "jellyfin",
			isOpenSubsonic = false
		)
	}

	override suspend fun currentUser(): DomainUser {
		val user = userApi.getUserById(userId).content
		return DomainUser(
			username = user.name ?: "Jellyfin User",
			roles = if (user.policy?.isAdministrator == true) setOf("admin") else emptySet()
		)
	}

	override suspend fun getAlbums(
		type: DomainAlbumListType,
		size: Int,
		offset: Int
	): List<DomainAlbum> {
		val sortByList = when (type) {
			is DomainAlbumListType.Newest -> listOf(ItemSortBy.DATE_CREATED)
			is DomainAlbumListType.Random -> listOf(ItemSortBy.RANDOM)
			is DomainAlbumListType.Frequent -> listOf(ItemSortBy.PLAY_COUNT)
			is DomainAlbumListType.Recent -> listOf(ItemSortBy.DATE_PLAYED)
			is DomainAlbumListType.AlphabeticalByArtist -> listOf(ItemSortBy.ALBUM_ARTIST)
			is DomainAlbumListType.Year -> listOf(ItemSortBy.PRODUCTION_YEAR)
			else -> listOf(ItemSortBy.SORT_NAME)
		}

		val sortOrder = if (type is DomainAlbumListType.Newest || type is DomainAlbumListType.Recent) {
			SortOrder.DESCENDING
		} else {
			SortOrder.ASCENDING
		}

		val result = libraryApi.getItems(
			userId = userId,
			includeItemTypes = listOf(BaseItemKind.MUSIC_ALBUM),
			limit = size,
			startIndex = offset,
			recursive = true,
			sortBy = sortByList,
			sortOrder = listOf(sortOrder),
			fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.ITEM_COUNTS)
		).content

		return result.items.map { it.toDomainAlbum() }
	}

	override suspend fun getAlbum(id: String): DomainAlbum {
		val albumDto = libraryApi.getItem(userId = userId, itemId = UUID.fromString(id)).content
		val songsResult = libraryApi.getItems(
			userId = userId,
			parentId = UUID.fromString(id),
			includeItemTypes = listOf(BaseItemKind.AUDIO),
			sortBy = listOf(ItemSortBy.PARENT_INDEX_NUMBER, ItemSortBy.INDEX_NUMBER),
			sortOrder = listOf(SortOrder.ASCENDING),
			fields = listOf(ItemFields.MEDIA_STREAMS, ItemFields.PRIMARY_IMAGE_ASPECT_RATIO)
		).content

		val songs = songsResult.items.map { it.toDomainSong() }
		return albumDto.toDomainAlbum(songs)
	}

	override suspend fun getSong(id: String): DomainSong {
		val item = libraryApi.getItem(userId = userId, itemId = UUID.fromString(id)).content
		return item.toDomainSong()
	}

	override suspend fun getArtists(): List<DomainArtist> {
		val result = libraryApi.getItems(
			userId = userId,
			includeItemTypes = listOf(BaseItemKind.MUSIC_ARTIST),
			recursive = true,
			fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.ITEM_COUNTS)
		).content
		return result.items.map { it.toDomainArtist() }
	}

	override suspend fun getArtistInfo(id: String): DomainArtistInfo {
		val artist = libraryApi.getItem(userId = userId, itemId = UUID.fromString(id)).content
		val musicBrainzId = artist.providerIds?.entries?.find { it.key.equals("MusicBrainzArtist", ignoreCase = true) }?.value
		return DomainArtistInfo(
			biography = artist.overview,
			musicBrainzId = musicBrainzId,
			lastFmUrl = null,
			smallImageUrl = getCoverArtUrl(id, "300"),
			mediumImageUrl = getCoverArtUrl(id, "600"),
			largeImageUrl = getCoverArtUrl(id, "1000"),
			similarArtists = emptyList()
		)
	}

	override suspend fun getAlbumInfo(id: String): DomainAlbumInfo {
		val album = libraryApi.getItem(userId = userId, itemId = UUID.fromString(id)).content
		val musicBrainzId = album.providerIds?.entries?.find { it.key.equals("MusicBrainzAlbum", ignoreCase = true) }?.value
		return DomainAlbumInfo(
			notes = album.overview,
			musicBrainzId = musicBrainzId,
			lastFmUrl = null,
			smallImageUrl = getCoverArtUrl(id, "300"),
			mediumImageUrl = getCoverArtUrl(id, "600"),
			largeImageUrl = getCoverArtUrl(id, "1000")
		)
	}

	override suspend fun getGenres(): List<DomainGenre> {
		val result = libraryApi.getItems(
			userId = userId,
			includeItemTypes = listOf(BaseItemKind.MUSIC_GENRE),
			recursive = true
		).content

		return result.items.map { dto ->
			DomainGenre(
				name = dto.name ?: "Unknown",
				albumCount = 0,
				songCount = dto.childCount ?: 0,
				coverArtIds = emptyList()
			)
		}
	}

	override suspend fun getPlaylists(): List<DomainPlaylist> {
		val result = libraryApi.getItems(
			userId = userId,
			includeItemTypes = listOf(BaseItemKind.PLAYLIST),
			recursive = true,
			fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.ITEM_COUNTS)
		).content
		return result.items.map { it.toDomainPlaylist() }
	}

	override suspend fun getPlaylist(id: String): DomainPlaylist {
		val playlistDto = libraryApi.getItem(userId = userId, itemId = UUID.fromString(id)).content
		val songsResult = libraryApi.getItems(
			userId = userId,
			parentId = UUID.fromString(id),
			includeItemTypes = listOf(BaseItemKind.AUDIO),
			fields = listOf(ItemFields.MEDIA_STREAMS)
		).content

		val songs = songsResult.items.map { it.toDomainSong() }
		return playlistDto.toDomainPlaylist(songs)
	}

	override suspend fun getSimilarSongs(id: String, count: Int): List<DomainSong> {
		val result = instantMixApi.getInstantMixFromItem(
			itemId = UUID.fromString(id),
			userId = userId,
			limit = count,
			fields = listOf(ItemFields.MEDIA_STREAMS)
		).content
		return result.items.map { it.toDomainSong() }
	}

	override suspend fun search(query: String): DomainSearchResult {
		val result = searchApi.getSearchHints(
			searchTerm = query,
			userId = userId,
			includeItemTypes = listOf(BaseItemKind.AUDIO, BaseItemKind.MUSIC_ALBUM, BaseItemKind.MUSIC_ARTIST)
		).content

		val hints = result.searchHints
		val songIds = hints.filter { it.type == BaseItemKind.AUDIO }.map { it.id }
		val albumIds = hints.filter { it.type == BaseItemKind.MUSIC_ALBUM }.map { it.id }
		val artistIds = hints.filter { it.type == BaseItemKind.MUSIC_ARTIST }.map { it.id }

		val songs = songIds.mapNotNull { id -> runCatching { getSong(id.toString()) }.getOrNull() }
		val albums = albumIds.mapNotNull { id -> runCatching { libraryApi.getItem(userId = userId, itemId = id).content.toDomainAlbum() }.getOrNull() }
		val artists = artistIds.mapNotNull { id -> runCatching { libraryApi.getItem(userId = userId, itemId = id).content.toDomainArtist() }.getOrNull() }

		return DomainSearchResult(
			artists = artists,
			albums = albums,
			songs = songs
		)
	}

	override suspend fun getLyrics(songId: String): ServerLyrics? {
		return null
	}

	override suspend fun star(id: String) {
		userDataApi.markFavoriteItem(userId = userId, itemId = UUID.fromString(id))
	}

	override suspend fun unstar(id: String) {
		userDataApi.unmarkFavoriteItem(userId = userId, itemId = UUID.fromString(id))
	}

	override suspend fun setRating(id: String, rating: Int) {
		userDataApi.updateUserItemRating(
			userId = userId,
			itemId = UUID.fromString(id),
			likes = rating > 3
		)
	}

	override suspend fun scrobble(id: String, submission: Boolean, time: Instant?) {
		val uuid = UUID.fromString(id)
		if (submission) {
			sessionApi.reportPlaybackStopped(
				PlaybackStopInfo(
					itemId = uuid,
					failed = false
				)
			)
		} else {
			sessionApi.reportPlaybackStart(
				PlaybackStartInfo(
					itemId = uuid,
					canSeek = true,
					isPaused = false,
					isMuted = false,
					playMethod = PlayMethod.DIRECT_PLAY,
					repeatMode = RepeatMode.REPEAT_NONE,
					playbackOrder = PlaybackOrder.DEFAULT
				)
			)
		}
	}

	override suspend fun reportPlayback(
		id: String,
		positionMs: Long,
		state: DomainPlaybackState,
		playbackRate: Float
	) {
		val uuid = UUID.fromString(id)
		val positionTicks = positionMs * 10_000L

		when (state) {
			DomainPlaybackState.PLAYING, DomainPlaybackState.STARTING -> {
				sessionApi.reportPlaybackProgress(
					PlaybackProgressInfo(
						itemId = uuid,
						positionTicks = positionTicks,
						isPaused = false,
						canSeek = true,
						isMuted = false,
						playMethod = PlayMethod.DIRECT_PLAY,
						repeatMode = RepeatMode.REPEAT_NONE,
						playbackOrder = PlaybackOrder.DEFAULT
					)
				)
			}
			DomainPlaybackState.PAUSED -> {
				sessionApi.reportPlaybackProgress(
					PlaybackProgressInfo(
						itemId = uuid,
						positionTicks = positionTicks,
						isPaused = true,
						canSeek = true,
						isMuted = false,
						playMethod = PlayMethod.DIRECT_PLAY,
						repeatMode = RepeatMode.REPEAT_NONE,
						playbackOrder = PlaybackOrder.DEFAULT
					)
				)
			}
			DomainPlaybackState.STOPPED -> {
				sessionApi.reportPlaybackStopped(
					PlaybackStopInfo(
						itemId = uuid,
						positionTicks = positionTicks,
						failed = false
					)
				)
			}
		}
	}

	override suspend fun createPlaylist(name: String, songIds: List<String>): DomainPlaylist {
		val playlistDto = playlistApi.createPlaylist(
			data = org.jellyfin.sdk.model.api.CreatePlaylistDto(
				name = name,
				ids = songIds.map { UUID.fromString(it) },
				userId = userId,
				users = emptyList(),
				isPublic = true
			)
		).content

		val createdId = playlistDto.id
		return getPlaylist(createdId)
	}

	override suspend fun updatePlaylist(
		id: String,
		name: String?,
		comment: String?,
		public: Boolean?,
		songIdsToAdd: List<String>,
		songIndicesToRemove: List<Int>
	) {
		val playlistUuid = UUID.fromString(id)
		if (songIdsToAdd.isNotEmpty()) {
			playlistApi.addItemToPlaylist(
				playlistId = playlistUuid,
				ids = songIdsToAdd.map { UUID.fromString(it) },
				userId = userId
			)
		}
	}

	override suspend fun deletePlaylist(id: String) {
		libraryApi.deleteItem(itemId = UUID.fromString(id))
	}

	override fun getStreamUrl(
		id: String,
		maxBitRate: Int,
		format: String?,
		estimateContentLength: Boolean
	): String {
		return "$baseUrl/Audio/$id/stream?static=true&api_key=$accessToken"
	}

	override suspend fun getPlaybackStreamUrl(
		id: String,
		bitrateKbps: Int,
		format: String?
	): String {
		val formattedFormat = format?.takeIf { it.isNotBlank() } ?: "mp3"
		return if (bitrateKbps > 0) {
			val bitrateBps = bitrateKbps * 1000
			"$baseUrl/Audio/$id/universal?maxStreamingBitrate=$bitrateBps&container=$formattedFormat&api_key=$accessToken"
		} else {
			getStreamUrl(id, maxBitRate = 0, format = format)
		}
	}

	override fun getCoverArtUrl(id: String, size: String?): String {
		val query = size?.let { "?fillWidth=$it&fillHeight=$it&quality=90" } ?: ""
		return "$baseUrl/Items/$id/Images/Primary$query"
	}

	override suspend fun getCoverArt(id: String, size: String?): ByteArray {
		val url = getCoverArtUrl(id, size)
		return java.net.URL(url).readBytes()
	}

	override fun getAvatarUrl(username: String): String {
		return "$baseUrl/Users/$userId/Images/Primary"
	}

	override fun parseResponseError(contentType: String?, body: String): ServerException? {
		return null
	}
}
