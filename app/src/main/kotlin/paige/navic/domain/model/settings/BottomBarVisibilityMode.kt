/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import androidx.annotation.StringRes
import paige.navic.R

enum class BottomBarVisibilityMode(@StringRes val displayName: Int) {
	Default(R.string.option_bottom_bar_visibility_mode_default),
	AllScreens(R.string.option_bottom_bar_visibility_mode_all_screens)
}
