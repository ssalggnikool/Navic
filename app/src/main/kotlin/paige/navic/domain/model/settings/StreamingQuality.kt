/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import androidx.compose.runtime.Composable
import paige.navic.R

enum class StreamingQuality(
	val displayName: Int,
	val bitrate: Int,
	val container: String?
) {
	Low(
		displayName = R.string.option_quality_low,
		bitrate = 80,
		container = "opus"
	),
	Medium(
		displayName = R.string.option_quality_medium,
		bitrate = 128,
		container = "opus"
	),
	High(
		displayName = R.string.option_quality_high,
		bitrate = 192,
		container = "opus"
	),
	Lossless(
		displayName = R.string.option_quality_lossless,
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
