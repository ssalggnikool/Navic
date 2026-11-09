/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DomainArtistInfo(
	val musicBrainzId: String?,
	val biography: String?,
	val smallImageUrl: String?,
	val mediumImageUrl: String?,
	val largeImageUrl: String?,
	val lastFmUrl: String?,
	val similarArtists: List<DomainArtist>
)
