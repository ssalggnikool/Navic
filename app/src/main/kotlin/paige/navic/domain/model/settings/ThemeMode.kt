/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R


enum class ThemeMode(val title: Int) {
	System(R.string.theme_mode_system),
	Dark(R.string.theme_mode_dark),
	Light(R.string.theme_mode_light)
}
