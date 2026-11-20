/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
sealed class DomainSongListType {
	@Serializable
	@Immutable
	data object FrequentlyPlayed : DomainSongListType()

	@Serializable
	@Immutable
	data object Newest : DomainSongListType()

	@Serializable
	@Immutable
	data object Random : DomainSongListType()

	@Serializable
	@Immutable
	data object Rating : DomainSongListType()

	@Serializable
	@Immutable
	data object Year : DomainSongListType()

	@Serializable
	@Immutable
	data class ByGenre(val genre: String) : DomainSongListType()

	@Serializable
	@Immutable
	data class ByArtist(val artistId: String) : DomainSongListType()
}
