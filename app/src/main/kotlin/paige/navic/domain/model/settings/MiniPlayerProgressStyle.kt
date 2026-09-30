/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class MiniPlayerProgressStyle(val displayName: Int) {
	Hidden(R.string.option_mini_player_progress_style_hidden),
	Visible(R.string.option_mini_player_progress_style_visible),
	Seekable(R.string.option_mini_player_progress_style_seekable)
}
