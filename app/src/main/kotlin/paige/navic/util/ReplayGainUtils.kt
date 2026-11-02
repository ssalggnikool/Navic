/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.settings.ReplayGainMode
import kotlin.math.pow

fun DomainReplayGain.effectiveGainAndPeak(mode: ReplayGainMode = ReplayGainMode.Track): Pair<Float, Float?>? =
	when (mode) {
		ReplayGainMode.Off -> null
		ReplayGainMode.Album -> albumGain?.withPeak(albumPeak)
			?: trackGain?.withPeak(trackPeak)
			?: (fallbackGain ?: baseGain)?.withPeak(null)
		ReplayGainMode.Track, ReplayGainMode.Dynamic -> trackGain?.withPeak(trackPeak)
			?: albumGain?.withPeak(albumPeak)
			?: (fallbackGain ?: baseGain)?.withPeak(null)
	}

private fun Float.withPeak(peak: Float?) = this to peak


fun Float.decibelsToLinear(): Float {
	return 10.0f.pow(this / 20.0f)
}
