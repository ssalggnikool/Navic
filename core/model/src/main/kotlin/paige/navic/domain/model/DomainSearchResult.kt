/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DomainSearchResult(
	val albums: List<DomainAlbum> = emptyList(),
	val artists: List<DomainArtist> = emptyList(),
	val songs: List<DomainSong> = emptyList()
)
