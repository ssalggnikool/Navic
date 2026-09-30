/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.lyrics

import kotlin.time.Duration

data class LyricsWord(
	val time: Duration,
	val duration: Duration,
	val text: String
)
