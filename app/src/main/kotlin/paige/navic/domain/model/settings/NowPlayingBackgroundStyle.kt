/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class NowPlayingBackgroundStyle(val displayName: Int) {
	Static(R.string.option_now_playing_background_style_static),
	Dynamic(R.string.option_now_playing_background_style_dynamic)
}
