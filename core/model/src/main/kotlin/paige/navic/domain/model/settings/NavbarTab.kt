/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class NavbarTab(
	val id: Id,
	val visible: Boolean
) {
	@Serializable
	enum class Id {
		LIBRARY,
		ALBUMS,
		PLAYLISTS,
		ARTISTS,
		SEARCH,
		GENRES,
		SONGS,
		RADIOS,
		STATISTICS
	}
}
