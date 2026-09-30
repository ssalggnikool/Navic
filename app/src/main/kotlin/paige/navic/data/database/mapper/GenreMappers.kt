/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.mapper

import paige.navic.data.database.entity.GenreEntity
import paige.navic.data.database.relation.GenreWithAlbums
import paige.navic.domain.model.DomainGenre
import dev.zt64.subsonic.api.model.Genre as ApiGenre

fun ApiGenre.toEntity() = GenreEntity(
	genreName = name,
	albumCount = albumCount,
	songCount = songCount
)

fun GenreWithAlbums.toDomainModel() = DomainGenre(
	name = genre.genreName,
	albumCount = genre.albumCount,
	songCount = genre.songCount,
	albums = albums.map { it.toDomainModel() }
)

fun DomainGenre.toEntity() = GenreEntity(
	genreName = name,
	albumCount = albumCount,
	songCount = songCount
)
