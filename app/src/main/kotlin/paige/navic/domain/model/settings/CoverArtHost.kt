/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import kotlinx.serialization.Serializable
import paige.navic.R

@Serializable
enum class CoverArtHost(val displayName: String, val needsApiKey: Boolean = false) {
	Catbox("Catbox"),
	Litterbox("Litterbox"),
	FreeImageHost("FreeImageHost"),
	ImgBB("ImgBB", true)
}
