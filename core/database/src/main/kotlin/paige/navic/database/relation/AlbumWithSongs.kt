/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.entity.SongEntity

data class AlbumWithSongs(
    @Embedded val album: AlbumEntity,
    @Relation(
		parentColumns = ["albumId"],
		entityColumns = ["belongsToAlbumId"]
	)
	val songs: List<SongEntity>
)
