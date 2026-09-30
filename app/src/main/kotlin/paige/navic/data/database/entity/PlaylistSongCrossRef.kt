/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
	primaryKeys = ["playlistId", "songId", "position"],
	indices = [Index(value = ["songId"])],
	foreignKeys = [
		ForeignKey(
			entity = PlaylistEntity::class,
			parentColumns = ["playlistId"],
			childColumns = ["playlistId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = SongEntity::class,
			parentColumns = ["songId"],
			childColumns = ["songId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class PlaylistSongCrossRef(
	val playlistId: String,
	val songId: String,
	val position: Int
)
