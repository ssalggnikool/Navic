/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.relation

import androidx.room3.Embedded
import paige.navic.database.entity.GenreEntity

data class GenreWithCovers(
	@Embedded val genre: GenreEntity,
	val firstCoverArtId: String?,
	val secondCoverArtId: String?
)
