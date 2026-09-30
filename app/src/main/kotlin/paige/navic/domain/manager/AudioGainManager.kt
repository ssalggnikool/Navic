/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import androidx.media3.common.util.UnstableApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.exoplayer.ExoStateHolder

@UnstableApi
class AudioGainManager: KoinComponent {
	private val stateHolder: ExoStateHolder by inject()

    fun applyGainMode(
        mode: ReplayGainMode
    ) {
		stateHolder.gainProcessor.applyGainMode(mode)
    }

	fun resetGain() {
		stateHolder.gainProcessor.resetGain()
    }

	fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float) {
		stateHolder.gainProcessor.rgAmpValue = withReplayGain
		stateHolder.gainProcessor.ampValue = withoutReplayGain
	}

	fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		stateHolder.gainProcessor.setReplayGainMetadata(metadata)
	}
}
