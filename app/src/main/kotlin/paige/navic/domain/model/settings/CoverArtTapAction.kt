/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class CoverArtTapAction(val displayName: Int) {
	Disabled(R.string.option_cover_art_action_disabled),
	ShowLyrics(R.string.option_cover_art_action_show_lyrics)
}
