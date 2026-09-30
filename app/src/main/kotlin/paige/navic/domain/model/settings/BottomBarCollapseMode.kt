/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class BottomBarCollapseMode(val displayName: Int) {
	Never(R.string.option_bottom_bar_collapse_mode_never),
	OnScroll(R.string.option_bottom_bar_collapse_mode_on_scroll)
}
