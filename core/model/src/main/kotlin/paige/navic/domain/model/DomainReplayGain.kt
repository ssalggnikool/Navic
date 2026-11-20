/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DomainReplayGain(
	val albumGain: Float?,
	val albumPeak: Float?,
	val trackGain: Float?,
	val trackPeak: Float?,
	val baseGain: Float?,
	val fallbackGain: Float?
)
