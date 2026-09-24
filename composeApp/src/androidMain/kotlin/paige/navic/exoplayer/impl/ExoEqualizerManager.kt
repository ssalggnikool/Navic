package paige.navic.exoplayer.impl

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import paige.navic.domain.manager.EqualiserManager
import paige.navic.domain.models.settings.EqualiserMode
import paige.navic.util.Logger

@UnstableApi
class ExoEqualizerManager(
    private val equaliserManager: EqualiserManager,
    private val context: Context
): Player.Listener {
	private var equalizerMode = EqualiserMode.Disabled
	private val scope = CoroutineScope(Dispatchers.Default)
	private var currentAudioSessionId = C.AUDIO_SESSION_ID_UNSET
	private var equaliser: Equalizer? = null

	override fun onAudioSessionIdChanged(audioSessionId: Int) {
		currentAudioSessionId = audioSessionId
		applyEqualiserMode(equalizerMode)
	}

	private fun createEqualiser(sessionId: Int) {
		releaseEqualiser()
		try {
			val equaliser = Equalizer(0, sessionId).apply {
				enabled = true
			}

			this.equaliser = equaliser

			val bandLowerRange = equaliser.bandLevelRange.firstOrNull()?.toFloat() ?: -1500f
			val bandUpperRange = equaliser.bandLevelRange.lastOrNull()?.toFloat() ?: 1500f
			val bandCount = equaliser.numberOfBands.toInt()

			scope.launch {
				equaliserManager.setConfig(
					equaliserManager.config.value.copy(
						bandLowerRange = bandLowerRange,
						bandUpperRange = bandUpperRange,
						bandCount = bandCount
					)
				)
			}

			updateEqualiser()
		} catch (ex: Exception) {
			Logger.e("PlaybackService", "error while configuring eq", ex)
		}
	}

	fun updateEqualiser() {
		val equaliser = equaliser ?: return
		val config = equaliserManager.config.value
		try {
			// reset all band levels first in case an item in
			// config.bandLevels was removed (e.g. user presses
			// reset in the equaliser settings)
			repeat(equaliser.numberOfBands.toInt()) { band ->
				equaliser.setBandLevel(band.toShort(), 0)
			}
			config.bandLevels.forEach { (band, level) ->
				equaliser.setBandLevel(band.toShort(), level.toInt().toShort())
			}
		} catch (ex: Exception) {
			Logger.e("PlaybackService", "error while setting eq band levels", ex)
		}
	}

	fun applyEqualiserMode(mode: EqualiserMode) {
		equalizerMode = mode
		closeAudioEffectSession(currentAudioSessionId)
		releaseEqualiser()

		when (mode) {
			EqualiserMode.BuiltIn -> createEqualiser(currentAudioSessionId)
			EqualiserMode.External -> openAudioEffectSession(currentAudioSessionId)
			EqualiserMode.Disabled -> Unit
		}
	}

	// Announces our audio session to the system so external equalizer apps can attach effects to it
	private fun openAudioEffectSession(sessionId: Int) {
		if (sessionId == C.AUDIO_SESSION_ID_UNSET) return
		currentAudioSessionId = sessionId

		context.sendBroadcast(
			Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
				putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
				putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
				putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
			}
		)
	}

	// Tells external equalizer apps our audio session is going away so they can release their effects
	private fun closeAudioEffectSession(sessionId: Int) {
		if (currentAudioSessionId == C.AUDIO_SESSION_ID_UNSET) return
		context.sendBroadcast(
			Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
				putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
				putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
			}
		)
		currentAudioSessionId = C.AUDIO_SESSION_ID_UNSET
	}

	fun releaseEqualiser() {
		this.equaliser = null
		closeAudioEffectSession(currentAudioSessionId)
	}
}
