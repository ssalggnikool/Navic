/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.GenreEntity
import paige.navic.database.relation.GenreWithCovers
import paige.navic.domain.model.DomainGenre

fun GenreEntity.toDomainModel(coverArtIds: List<String> = emptyList()) = DomainGenre(
	name = genreName,
	albumCount = albumCount,
	songCount = songCount,
	coverArtIds = coverArtIds
)

fun GenreWithCovers.toDomainModel() = genre.toDomainModel(
	coverArtIds = listOfNotNull(firstCoverArtId, secondCoverArtId)
)

fun DomainGenre.toEntity() = GenreEntity(
    genreName = name,
    albumCount = albumCount,
    songCount = songCount
)
