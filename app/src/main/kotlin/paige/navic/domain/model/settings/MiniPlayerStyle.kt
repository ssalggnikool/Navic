/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class MiniPlayerStyle(val displayName: Int) {
	Unified(R.string.option_mini_player_style_unified),
	Detached(R.string.option_mini_player_style_detached)
}
