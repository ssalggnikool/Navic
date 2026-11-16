/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

enum class SyncActionType {
	STAR, UNSTAR, DELETE_PLAYLIST, SCROBBLE,

	// this is dumb but it works so whatever
	STAR_0, STAR_1, STAR_2, STAR_3, STAR_4, STAR_5,

	CREATE_PLAYLIST
}

@Serializable
data class CreatePlaylistContent(
	val name: String,
	val songIds: List<String>
)

@Entity
data class SyncActionEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val actionType: SyncActionType,
	val itemId: String,
	val content: String? = null,
	val time: Instant = Clock.System.now()
)
