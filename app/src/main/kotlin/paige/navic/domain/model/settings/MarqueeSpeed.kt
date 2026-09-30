/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

enum class MarqueeSpeed(val value: Int) {
	Disabled(0),
	Slow(6000),
	Medium(4000),
	Fast(1000)
}
