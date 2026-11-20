/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.preferences

import com.russhwolf.settings.Settings
import paige.navic.preferences.base.BasePreferenceManager

/**
 * Preferences that belong to the active server account rather than the app.
 */
public class AccountSettings(
	settings: Settings,
	keyPrefix: String
) : BasePreferenceManager(settings, keyPrefix) {
	var customHeaders by preference(CUSTOM_HEADERS)
	var alternateServerAddresses by jsonPreference(ALTERNATE_ADDRESSES, emptyList<String>())

	fun customHeadersMap(): Map<String, String> = buildMap {
		for (line in customHeaders.lines()) {
			val parts = line.split(":", limit = 2)
			if (parts.size < 2) continue

			val rawKey = parts[0]
			val rawValue = parts[1]

			val key = rawKey.trim()
			val value = rawValue.trim()
			if (key.isNotEmpty() && value.isNotEmpty()) put(key, value)
		}
	}

	/**
	 * Removes the scoped keys of an account. The prefix has to be passed in since
	 * this manager only knows the prefix of the active account.
	 */
	fun clearAccount(prefix: String) {
		settings.remove(prefix + CUSTOM_HEADERS)
		settings.remove(prefix + ALTERNATE_ADDRESSES)
	}

	private companion object {
		const val CUSTOM_HEADERS = "customHeaders"
		const val ALTERNATE_ADDRESSES = "alternateServerAddresses"
	}
}
