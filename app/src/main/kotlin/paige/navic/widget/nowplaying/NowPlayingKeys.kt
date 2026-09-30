/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.widget.nowplaying

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object NowPlayingKeys {
	val isPlaying = booleanPreferencesKey("is_playing")
	val titleKey = stringPreferencesKey("title")
	val artistKey = stringPreferencesKey("artist")
	val artUrlKey = stringPreferencesKey("art_url")
}
