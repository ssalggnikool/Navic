/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.entity.PlaylistSongCrossRef
import paige.navic.database.entity.SongEntity

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
		entity = PlaylistSongCrossRef::class,
		parentColumns = ["playlistId"],
		entityColumns = ["playlistId"]
	)
	val songs: List<PlaylistSong>
)

data class PlaylistSong(
    @Embedded val crossRef: PlaylistSongCrossRef,
    @Relation(
		parentColumns = ["songId"],
		entityColumns = ["songId"]
	)
	val song: SongEntity
)
