/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin.mapper

import org.jellyfin.sdk.model.api.BaseItemDto
import paige.navic.domain.model.DomainArtist
import kotlin.time.Instant

fun BaseItemDto.toDomainArtist(): DomainArtist {
	val artistId = id.toString()
	val isStarred = userData?.isFavorite == true
	val starredInstant = if (isStarred) Instant.fromEpochMilliseconds(0) else null

	return DomainArtist(
		id = artistId,
		name = name ?: "Unknown Artist",
		albumCount = childCount ?: 0,
		coverArtId = artistId,
		artistImageUrl = null,
		starredAt = starredInstant,
		userRating = userData?.rating?.toInt(),
		sortName = sortName,
		musicBrainzId = providerIds?.get("MusicBrainzArtist"),
		lastFmUrl = null,
		roles = emptyList(),
		biography = overview,
		similarArtistIds = emptyList()
	)
}
