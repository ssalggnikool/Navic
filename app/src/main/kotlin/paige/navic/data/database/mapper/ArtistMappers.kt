/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.mapper

import paige.navic.data.database.entity.ArtistEntity
import paige.navic.domain.model.DomainArtist
import dev.zt64.subsonic.api.model.Artist as ApiArtist

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

fun ArtistEntity.toDomainModel() = DomainArtist(
	id = this.artistId,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = this.lastFmUrl,
	roles = this.roles,
	biography = this.biography,
	similarArtistIds = this.similarArtistIds
)

fun DomainArtist.toEntity() = ArtistEntity(
	artistId = this.id,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = this.lastFmUrl,
	roles = this.roles,
	biography = this.biography,
	similarArtistIds = this.similarArtistIds
)
