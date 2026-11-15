/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ServerLyrics(
	val lang: String?,
	val synced: Boolean,
	val offsetMillis: Int,
	val lines: List<ServerLyricLine>
)

@Immutable
data class ServerLyricLine(
	val startMillis: Long?,
	val text: String
)
