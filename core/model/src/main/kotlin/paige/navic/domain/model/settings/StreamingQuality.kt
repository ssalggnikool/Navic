/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import androidx.compose.runtime.Composable

enum class StreamingQuality(
	val bitrate: Int,
	val container: String?
) {
	Low(
		bitrate = 80,
		container = "opus"
	),
	Medium(
		bitrate = 128,
		container = "opus"
	),
	High(
		bitrate = 192,
		container = "opus"
	),
	Lossless(
		bitrate = 0,
		container = null
	)
}

@Composable
fun StreamingQuality.description(): String? {
	return if (container != null) {
		"${bitrate}kbps, ${container.uppercase()}"
	} else {
		null
	}
}
