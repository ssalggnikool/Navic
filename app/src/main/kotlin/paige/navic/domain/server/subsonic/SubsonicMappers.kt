/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic

import paige.navic.database.entity.AlbumEntity
import paige.navic.database.entity.ArtistEntity
import paige.navic.database.entity.GenreEntity
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.entity.RadioEntity
import paige.navic.database.entity.SongEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistInfo
import paige.navic.domain.model.DomainChatMessage
import paige.navic.domain.model.DomainContributor
import paige.navic.domain.model.DomainExplicitStatus
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.DomainSearchResult
import paige.navic.domain.model.DomainShare
import paige.navic.domain.model.DomainShareItem
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongArtist
import paige.navic.domain.model.DomainUser
import paige.navic.domain.model.ServerLyricLine
import paige.navic.domain.model.ServerLyrics
import kotlin.time.Duration.Companion.seconds
import dev.zt64.subsonic.api.model.Album as ApiAlbum
import dev.zt64.subsonic.api.model.AlbumInfo as ApiAlbumInfo
import dev.zt64.subsonic.api.model.AlbumListType as ApiAlbumListType
import dev.zt64.subsonic.api.model.Artist as ApiArtist
import dev.zt64.subsonic.api.model.ArtistInfo as ApiArtistInfo
import dev.zt64.subsonic.api.model.ChatMessage as ApiChatMessage
import dev.zt64.subsonic.api.model.Genre as ApiGenre
import dev.zt64.subsonic.api.model.InternetRadioStation as ApiRadio
import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist
import dev.zt64.subsonic.api.model.SearchResult as ApiSearchResult
import dev.zt64.subsonic.api.model.Share as ApiShare
import dev.zt64.subsonic.api.model.Song as ApiSong
import dev.zt64.subsonic.api.model.StructuredLyrics as ApiStructuredLyrics
import dev.zt64.subsonic.api.model.SubsonicResource as ApiSubsonicResource
import dev.zt64.subsonic.api.model.User as ApiUser

fun ApiSong.toEntity(
	artistIdOverride: String? = null,
	artistNameOverride: String? = null
) = SongEntity(
	songId = this.id,
	title = this.title,
	artistName = artistNameOverride ?: this.artistName,
	// TODO: figure out why this can be null and how to handle it
	artistId = artistIdOverride ?: this.artistId ?: "unknown artist",
	albumTitle = this.albumTitle,
	belongsToAlbumId = this.albumId,
	coverArtId = this.coverArtId,
	duration = this.duration ?: 0.seconds,
	trackNumber = this.trackNumber,
	discNumber = this.discNumber,
	year = this.year,
	genre = this.genre,
	bitRate = this.bitRate,
	mimeType = this.mimeType,
	fileExtension = this.fileExtension,
	filePath = this.filePath,
	starredAt = this.starredAt,
	parentId = this.parentId,
	genres = this.genres,
	moods = this.moods,
	isrc = this.isrc,
	bpm = this.bpm,
	comment = this.comment,
	playCount = this.playCount,
	userRating = this.userRating,
	averageRating = this.averageRating,
	bitDepth = this.bitDepth,
	sampleRate = this.sampleRate,
	audioChannelCount = this.audioChannelCount,
	fileSize = this.fileSize ?: 0L,
	musicBrainzId = this.musicBrainzId,
	contributors = this.contributors.map {
		DomainContributor(
			role = it.role,
			subRole = it.subRole,
			artistId = it.artist.id,
			artistName = it.artist.name
		)
	},
	replayGain = this.replayGain?.let {
		DomainReplayGain(
			albumGain = it.albumGain,
			albumPeak = it.albumPeak,
			trackGain = it.trackGain,
			trackPeak = it.trackPeak,
			baseGain = it.baseGain,
			fallbackGain = it.fallbackGain
		)
	},
	explicitStatus = when (this.explicitStatus) {
		ApiSong.ExplicitStatus.EXPLICIT -> DomainExplicitStatus.Explicit
		ApiSong.ExplicitStatus.CLEAN -> DomainExplicitStatus.Clean
		else -> DomainExplicitStatus.Unknown
	},
	artists = this.artists.map {
		DomainSongArtist(
			id = it.id,
			name = it.name
		)
	},
	albumArtists = this.albumArtists.map {
		DomainSongArtist(
			id = it.id,
			name = it.name
		)
	},
	isExternal = this.isExternal
)

fun ApiAlbum.toEntity(
	artistIdOverride: String? = null,
	artistNameOverride: String? = null
) = AlbumEntity(
	albumId = this.id,
	name = this.name,
	// TODO: figure out why this can be null and how to handle it
	artistId = artistIdOverride ?: this.artistId ?: "unknown artist",
	artistName = artistNameOverride ?: this.artistName,
	coverArtId = this.coverArtId,
	songCount = this.songCount,
	duration = this.duration,
	year = this.year,
	genre = this.genre,
	starredAt = this.starredAt,
	userRating = this.userRating,
	musicBrainzId = this.musicBrainzId,
	createdAt = this.createdAt,
	lastPlayedAt = this.lastPlayedAt,
	playCount = this.playCount,
	genres = this.genres,
	version = this.version,
	isExternal = this.isExternal,
	displayArtist = this.displayArtist
)

fun ApiArtist.toEntity() = ArtistEntity(
	artistId = this.id,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = null,
	roles = this.roles,
	biography = null,
	similarArtistIds = emptyList()
)

fun ApiGenre.toEntity() = GenreEntity(
	genreName = name,
	albumCount = albumCount,
	songCount = songCount
)

fun ApiPlaylist.toEntity() = PlaylistEntity(
	playlistId = id,
	name = name,
	owner = owner,
	comment = comment,
	coverArtId = coverArtId,
	songCount = songCount,
	duration = duration,
	createdAt = createdAt,
	modifiedAt = modifiedAt,
	public = public,
	readOnly = readOnly,
	allowedUsers = allowedUsers,
	validUntil = validUntil
)

fun ApiRadio.toEntity() = RadioEntity(
	radioId = id,
	coverArtId = coverArtId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)

fun ApiAlbum.toDomainModel(includeSongs: Boolean = false): DomainAlbum =
	toEntity().toDomainModel(
		songs = if (includeSongs) {
			songs.map { it.toEntity().toDomainModel() }
		} else {
			emptyList()
		}
	)

fun ApiPlaylist.toDomainModel(includeSongs: Boolean = false): DomainPlaylist =
	toEntity().toDomainModel(
		songs = if (includeSongs) {
			songs.map { it.toEntity().toDomainModel() }
		} else {
			emptyList()
		}
	)

fun ApiArtist.toDomainModel(): DomainArtist = toEntity().toDomainModel()

fun ApiGenre.toDomainModel() = toEntity().toDomainModel()

fun ApiRadio.toDomainModel() = toEntity().toDomainModel()

fun ApiSong.toDomainModel(): DomainSong = toEntity().toDomainModel()

fun ApiAlbumInfo.toDomainModel() = DomainAlbumInfo(
	musicBrainzId = musicBrainzId,
	largeImageUrl = largeImageUrl,
	mediumImageUrl = mediumImageUrl,
	smallImageUrl = smallImageUrl,
	lastFmUrl = lastFmUrl,
	notes = notes
)

fun ApiArtistInfo.toDomainModel() = DomainArtistInfo(
	musicBrainzId = musicBrainzId,
	biography = biography,
	smallImageUrl = smallImageUrl,
	mediumImageUrl = mediumImageUrl,
	largeImageUrl = largeImageUrl,
	lastFmUrl = lastFmUrl,
	similarArtists = similarArtists.map { it.toDomainModel() }
)

fun ApiUser.toDomainModel() = DomainUser(
	username = name,
	email = email,
	scrobblingEnabled = scrobblingEnabled,
	roles = roles.map { it.name.lowercase() }.toSet()
)

fun ApiSearchResult.toDomainModel() = DomainSearchResult(
	albums = albums.map { it.toDomainModel() },
	artists = artists.map { it.toDomainModel() },
	songs = songs.map { it.toDomainModel() }
)

fun ApiChatMessage.toDomainModel() = DomainChatMessage(
	username = username,
	time = time,
	message = message
)

fun ApiStructuredLyrics.toDomainModel() = ServerLyrics(
	lang = lang,
	synced = synced,
	offsetMillis = offset,
	lines = lines.map { ServerLyricLine(startMillis = it.start?.toLong(), text = it.value) }
)

fun ApiSubsonicResource.toDomainModel() = DomainShareItem(
	id = id,
	coverArtId = coverArtId
)

fun ApiShare.toDomainModel() = DomainShare(
	id = id,
	url = url,
	description = description,
	username = username,
	createdAt = createdAt,
	expiresAt = expiresAt,
	lastVisited = lastVisited,
	visitCount = visitCount,
	items = items.map { it.toDomainModel() }
)

fun DomainAlbumListType.toApi(): ApiAlbumListType = when (this) {
	DomainAlbumListType.Random -> ApiAlbumListType.Random
	DomainAlbumListType.Newest -> ApiAlbumListType.Newest
	DomainAlbumListType.Highest -> ApiAlbumListType.Highest
	DomainAlbumListType.Frequent -> ApiAlbumListType.Frequent
	DomainAlbumListType.Recent -> ApiAlbumListType.Recent
	DomainAlbumListType.AlphabeticalByName -> ApiAlbumListType.AlphabeticalByName
	DomainAlbumListType.AlphabeticalByArtist -> ApiAlbumListType.AlphabeticalByArtist
	DomainAlbumListType.Year -> ApiAlbumListType.AlphabeticalByName
	is DomainAlbumListType.ByYear -> ApiAlbumListType.ByYear(fromYear, toYear)
	is DomainAlbumListType.ByGenre -> ApiAlbumListType.ByGenre(genre)
}
