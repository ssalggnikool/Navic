/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin.mapper

import org.jellyfin.sdk.model.api.BaseItemDto
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainSong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

fun BaseItemDto.toDomainAlbum(songs: List<DomainSong> = emptyList()): DomainAlbum {
	val albumId = id.toString()
	val durationMs = runTimeTicks?.let { it / 10_000L } ?: 0L

	val isStarred = userData?.isFavorite == true
	val starredInstant = if (isStarred) Instant.fromEpochMilliseconds(0) else null

	return DomainAlbum(
		id = albumId,
		name = name ?: "Unknown Album",
		artistName = albumArtist ?: artists?.firstOrNull(),
		artistId = albumArtists?.firstOrNull()?.id?.toString() ?: albumId,
		displayArtist = albumArtist ?: artists?.firstOrNull(),
		year = productionYear,
		coverArtId = albumId,
		genre = genres?.firstOrNull(),
		genres = genres ?: emptyList(),
		songCount = childCount ?: songs.size,
		duration = durationMs.milliseconds,
		createdAt = Instant.fromEpochMilliseconds(0),
		starredAt = starredInstant,
		lastPlayedAt = null,
		playCount = userData?.playCount ?: 0,
		userRating = userData?.rating?.toInt(),
		version = null,
		musicBrainzId = providerIds?.get("MusicBrainzAlbum"),
		songs = songs
	)
}
