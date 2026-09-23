package paige.navic.domain.manager

import androidx.media3.common.util.UnstableApi
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.exoplayer.impl.ExoAudioGainProcessor

@UnstableApi
actual class AudioGainManager(
	private val exoAudioGainProcessor: ExoAudioGainProcessor
) {
    actual fun applyGainMode(
        mode: ReplayGainMode
    ) {
		exoAudioGainProcessor.applyGainMode(mode)
    }

    actual fun resetGain() {
		exoAudioGainProcessor.resetGain()
    }

	actual fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float) {
		exoAudioGainProcessor.rgAmpValue = withReplayGain
		exoAudioGainProcessor.ampValue = withoutReplayGain
	}

	actual fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		exoAudioGainProcessor.setReplayGainMetadata(metadata)
	}
}
