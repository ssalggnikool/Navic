package paige.navic.domain.manager

import androidx.media3.common.util.UnstableApi
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.exoplayer.ExoStateHolder
import paige.navic.exoplayer.impl.ExoAudioGainProcessor

@UnstableApi
actual class AudioGainManager(
	private val stateHolder: ExoStateHolder
) {
    actual fun applyGainMode(
        mode: ReplayGainMode
    ) {
		stateHolder.gainProcessor.applyGainMode(mode)
    }

    actual fun resetGain() {
		stateHolder.gainProcessor.resetGain()
    }

	actual fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float) {
		stateHolder.gainProcessor.rgAmpValue = withReplayGain
		stateHolder.gainProcessor.ampValue = withoutReplayGain
	}

	actual fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		stateHolder.gainProcessor.setReplayGainMetadata(metadata)
	}
}
