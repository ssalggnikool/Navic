/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.playback.exoplayer.ExoStateHolder
import paige.navic.playback.exoplayer.impl.playback.ExoAudioGainProcessor

@OptIn(UnstableApi::class)
class AudioGainManager : KoinComponent {
	private val stateHolder: ExoStateHolder by inject()

	fun applyGainMode(
		mode: ReplayGainMode
	) {
		onGainProcessors { it.applyGainMode(mode) }
	}

	fun resetGain() {
		onGainProcessors { it.resetGain() }
	}

	fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float) {
		onGainProcessors {
			it.rgAmpValue = withReplayGain
			it.ampValue = withoutReplayGain
		}
	}

	fun setPreventClipping(value: Boolean) {
		onGainProcessors { it.preventClipping = value }
	}

	fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		onGainProcessors { it.setReplayGainMetadata(metadata) }
	}

	private inline fun onGainProcessors(block: (ExoAudioGainProcessor) -> Unit) {
		block(stateHolder.gainProcessor)
		block(stateHolder.secondaryGainProcessor)
	}
}
