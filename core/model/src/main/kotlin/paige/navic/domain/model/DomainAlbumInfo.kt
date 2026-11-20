/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DomainAlbumInfo(
	val musicBrainzId: String?,
	val largeImageUrl: String?,
	val mediumImageUrl: String?,
	val smallImageUrl: String?,
	val lastFmUrl: String?,
	val notes: String?
)
