/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin.mapper

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaStreamType
import paige.navic.domain.model.DomainExplicitStatus
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongArtist
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

fun BaseItemDto.toDomainSong(): DomainSong {
	val songId = id.toString()
	val mainArtistName = artists?.firstOrNull() ?: albumArtist
	val mainArtistId = artistItems?.firstOrNull()?.id?.toString() ?: songId

	val audioStream = mediaStreams?.firstOrNull { it.type == MediaStreamType.AUDIO }

	val durationMs = runTimeTicks?.let { it / 10_000L } ?: 0L

	val songArtists = artistItems?.map {
		DomainSongArtist(
			id = it.id.toString(),
			name = it.name ?: ""
		)
	} ?: mainArtistName?.let {
		listOf(DomainSongArtist(id = mainArtistId, name = it))
	} ?: emptyList()

	val isStarred = userData?.isFavorite == true
	val starredInstant = if (isStarred) Instant.fromEpochMilliseconds(0) else null

	return DomainSong(
		id = songId,
		title = name ?: "Unknown Title",
		artistName = mainArtistName,
		artistId = mainArtistId,
		albumTitle = album,
		albumId = albumId?.toString(),
		parentId = parentId?.toString(),
		comment = overview,
		trackNumber = indexNumber,
		discNumber = parentIndexNumber,
		isrc = emptyList(),
		year = productionYear,
		genre = genres?.firstOrNull(),
		genres = genres ?: emptyList(),
		moods = emptyList(),
		duration = durationMs.milliseconds,
		bpm = null,
		contributors = emptyList(),
		playCount = userData?.playCount ?: 0,
		userRating = userData?.rating?.toInt(),
		averageRating = communityRating,
		bitRate = audioStream?.bitRate?.let { it / 1000 },
		bitDepth = audioStream?.bitDepth,
		sampleRate = audioStream?.sampleRate,
		audioChannelCount = audioStream?.channels,
		replayGain = null,
		fileSize = 0L,
		fileExtension = container,
		mimeType = container?.let { "audio/$it" },
		filePath = path,
		starredAt = starredInstant,
		coverArtId = id.toString(),
		musicBrainzId = providerIds?.get("MusicBrainzTrack"),
		explicitStatus = DomainExplicitStatus.Clean,
		artists = songArtists,
		albumArtists = emptyList()
	)
}
