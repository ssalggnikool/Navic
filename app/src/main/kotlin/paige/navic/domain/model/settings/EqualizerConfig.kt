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
data class EqualizerConfig(
	val bandLevels: Map<Int, Float> = emptyMap(),
	val bandCount: Int = 0,
	val bandLowerRange: Float = 0f,
	val bandUpperRange: Float = 0f,
	val mode: EqualizerMode = EqualizerMode.Disabled
)
