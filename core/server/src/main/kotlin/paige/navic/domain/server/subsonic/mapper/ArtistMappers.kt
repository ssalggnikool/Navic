/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.Artist as ApiArtist
import dev.zt64.subsonic.api.model.ArtistInfo as ApiArtistInfo
import paige.navic.database.entity.ArtistEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistInfo

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

fun ApiArtist.toDomainModel(): DomainArtist = toEntity().toDomainModel()

fun ApiArtistInfo.toDomainModel() = DomainArtistInfo(
	musicBrainzId = musicBrainzId,
	biography = biography,
	smallImageUrl = smallImageUrl,
	mediumImageUrl = mediumImageUrl,
	largeImageUrl = largeImageUrl,
	lastFmUrl = lastFmUrl,
	similarArtists = similarArtists.map { it.toDomainModel() }
)
