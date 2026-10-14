/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import paige.navic.R

@Immutable
enum class DomainPlaylistListType(val displayName: Int) {
	Name(R.string.option_sort_playlist_by_name),
	DateAdded(R.string.option_sort_playlist_date_added),
	Duration(R.string.option_sort_playlist_duration),
	Random(R.string.option_sort_random)
}
