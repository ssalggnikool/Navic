/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.lyrics

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

@Immutable
data class LyricsLine(
	val time: Duration? = null,
	val text: String,
	val words: List<LyricsWord>? = null
)
