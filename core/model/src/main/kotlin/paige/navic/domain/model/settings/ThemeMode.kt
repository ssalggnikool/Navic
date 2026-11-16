/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.util.PlatformSupport


enum class ThemeMode {
	System,
	Dark,
	Light;

	companion object {
		fun getDefaultMode(): ThemeMode {
			return if (!PlatformSupport.SUPPORTS_DARK_THEME) {
				Light
			} else {
				System
			}
		}
	}
}
