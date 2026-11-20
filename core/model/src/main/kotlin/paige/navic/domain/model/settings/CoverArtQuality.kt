/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

enum class CoverArtQuality(
	val value: Int
) {
	Low(512),
	Medium(1024),
	High(4096)
}
