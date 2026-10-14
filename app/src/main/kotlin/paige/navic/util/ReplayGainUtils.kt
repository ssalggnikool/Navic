/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.settings.ReplayGainMode
import kotlin.math.pow

fun DomainReplayGain.effectiveGain(mode: ReplayGainMode = ReplayGainMode.Track): Float? {
	return when (mode) {
		ReplayGainMode.Off -> null
		ReplayGainMode.Album -> albumGain ?: trackGain ?: fallbackGain ?: baseGain
		ReplayGainMode.Track, ReplayGainMode.Dynamic -> trackGain ?: albumGain ?: fallbackGain ?: baseGain
	}
}

fun Float.decibelsToLinear(): Float {
	return 10.0f.pow(this / 20.0f)
}
