/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class OfflineMode(val displayName: Int) {
	Auto(R.string.option_offline_mode_auto),
	Forced(R.string.option_offline_mode_forced),
	NoWiFi(R.string.option_offline_mode_no_wifi),
}
