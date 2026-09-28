package paige.navic.domain.manager

import androidx.media3.common.util.UnstableApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
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
