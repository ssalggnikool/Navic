/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

enum class AppIconVariant(
	val activityName: String,
	val designer: String
) {
	Default("MainActivityDefault", designer = "ssalggnikool"),
	Inverted("MainActivityInverted", designer = "ssalggnikool")
}
