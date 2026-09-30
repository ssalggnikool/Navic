/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.lyrics

import kotlinx.serialization.Serializable

@Serializable
data class LyricsProvider(
	val id: Id,
	val enabled: Boolean
) {
	@Serializable
	enum class Id {
		SUBSONIC,
		LYRICS_PLUS,
		LRCLIB
	}
}
