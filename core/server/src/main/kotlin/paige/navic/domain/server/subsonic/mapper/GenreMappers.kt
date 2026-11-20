/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.Genre as ApiGenre
import paige.navic.database.entity.GenreEntity
import paige.navic.database.mapper.toDomainModel

fun ApiGenre.toEntity() = GenreEntity(
	genreName = name,
	albumCount = albumCount,
	songCount = songCount
)

fun ApiGenre.toDomainModel() = toEntity().toDomainModel()
