/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CoverArtProvider(
	val host: CoverArtHost,
	val enabled: Boolean = false
)

@Immutable
@Serializable
data class CoverArtProvidersConfig(
	val providers: List<CoverArtProvider>,
	val version: Int
) {
	companion object {
		const val VERSION = 1
		val Default = CoverArtProvidersConfig(
			providers = CoverArtHost.entries.map { CoverArtProvider(it) },
			version = VERSION
		)
	}
}
