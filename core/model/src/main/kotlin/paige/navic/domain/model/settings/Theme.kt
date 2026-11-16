/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.util.PlatformSupport

/**
 * Theme choices that the user can choose from
 */
enum class Theme {

	/**
	 * The app will be themed based on a "seed" colour.
	 *
	 * When this is selected, `accentColor(H/S/V)` settings
	 * will be exposed in the UI as a colour picker.
	 */
	Seeded,

	/**
	 * The app will be themed based on whatever the user
	 * chose in system settings. Only avaliable on Android S (API 31) and up.
	 */
	Dynamic,

	/**
	 * The app will be themed according to Apple's HIG.
	 */
	@Suppress("EnumEntryName")
	iOS,

	/**
	 * The same as iOS, but with a pink-ish accent.
	 */
	AppleMusic,

	/**
	 * The same as iOS, but with a green accent.
	 */
	Spotify;

	companion object {
		fun getDefaultTheme(): Theme {
			return if (PlatformSupport.SUPPORTS_DYNAMIC_THEME) {
				Dynamic
			} else {
				Seeded
			}
		}
	}

	fun isMaterialLike(): Boolean = when (this) {
		Dynamic,
		Seeded -> true

		else -> false
	}
}
