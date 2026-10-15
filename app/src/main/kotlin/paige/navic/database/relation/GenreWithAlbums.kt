/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.entity.GenreEntity

data class GenreWithAlbums(
    @Embedded val genre: GenreEntity,
    @Relation(
		entity = AlbumEntity::class,
		parentColumns = ["genreName"],
		entityColumns = ["genre"]
	)
	val albums: List<AlbumWithSongs>
)
