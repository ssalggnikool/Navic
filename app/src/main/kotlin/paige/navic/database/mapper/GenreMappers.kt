/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.GenreEntity
import paige.navic.database.relation.GenreWithCovers
import paige.navic.domain.model.DomainGenre
import dev.zt64.subsonic.api.model.Genre as ApiGenre

fun ApiGenre.toEntity() = GenreEntity(
    genreName = name,
    albumCount = albumCount,
    songCount = songCount
)

fun GenreWithCovers.toDomainModel() = DomainGenre(
	name = genre.genreName,
	albumCount = genre.albumCount,
	songCount = genre.songCount,
	coverArtIds = listOfNotNull(firstCoverArtId, secondCoverArtId)
)

fun DomainGenre.toEntity() = GenreEntity(
    genreName = name,
    albumCount = albumCount,
    songCount = songCount
)
