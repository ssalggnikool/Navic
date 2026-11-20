/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige, Nightdavisao
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

object PlaybackUtils {
	private const val MIMETYPE_PREFIX = "audio/"

	private val CORRECTED_CODEC_NAME = mapOf(
		"mp4a-latm" to "aac",
		"mpeg" to "mp3",
		"raw" to "wav"
	)

	fun getPrettyFormatName(mimeType: String): String {
		val name = mimeType.lowercase().removePrefix(MIMETYPE_PREFIX)
		return CORRECTED_CODEC_NAME.getOrDefault(name, name)
	}
}
