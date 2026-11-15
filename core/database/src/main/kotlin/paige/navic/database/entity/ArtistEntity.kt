/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(
	indices = [
		Index(value = ["starredAt"])
	]
)
data class ArtistEntity(
	@PrimaryKey val artistId: String,
	val name: String,
	val albumCount: Int = 0,
	val coverArtId: String? = null,
	val artistImageUrl: String? = null,
	val starredAt: Instant? = null,
	val userRating: Int? = null,
	val sortName: String? = null,
	val musicBrainzId: String? = null,
	val lastFmUrl: String? = null,
	val roles: List<String> = emptyList(),
	val biography: String? = null,
	val similarArtistIds: List<String> = emptyList()
)
