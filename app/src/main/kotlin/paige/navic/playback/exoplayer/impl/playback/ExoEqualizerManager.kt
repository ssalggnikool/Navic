/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import paige.navic.domain.manager.playback.EqualizerManager
import paige.navic.domain.model.settings.EqualizerMode
import paige.navic.util.Logger

@OptIn(UnstableApi::class)
class ExoEqualizerManager(
	private val equalizerManager: EqualizerManager,
	private val context: Context
) : Player.Listener {
	private var equalizerMode = EqualizerMode.Disabled
	private val scope = CoroutineScope(Dispatchers.Default)
	private var currentAudioSessionId = C.AUDIO_SESSION_ID_UNSET
	private var equalizer: Equalizer? = null

	override fun onAudioSessionIdChanged(audioSessionId: Int) {
		releaseEqualizer()
		currentAudioSessionId = audioSessionId
		applyEqualizerMode(equalizerMode, closeCurrentSession = false)
	}

	private fun createEqualizer(sessionId: Int) {
		if (sessionId == C.AUDIO_SESSION_ID_UNSET) return
		try {
			val equalizer = Equalizer(0, sessionId).apply {
				enabled = true
			}

			this.equalizer = equalizer

			val bandLowerRange = equalizer.bandLevelRange.firstOrNull()?.toFloat() ?: -1500f
			val bandUpperRange = equalizer.bandLevelRange.lastOrNull()?.toFloat() ?: 1500f
			val bandCount = equalizer.numberOfBands.toInt()

			scope.launch {
				equalizerManager.setConfig(
					equalizerManager.config.value.copy(
						bandLowerRange = bandLowerRange,
						bandUpperRange = bandUpperRange,
						bandCount = bandCount
					)
				)
			}

			updateEqualizer()
		} catch (ex: Exception) {
			Logger.e("ExoEqualizerManager", "error while configuring eq", ex)
		}
	}

	fun updateEqualizer() {
		val equalizer = this.equalizer ?: return
		val config = equalizerManager.config.value
		try {
			// reset all band levels first in case an item in
			// config.bandLevels was removed (e.g. user presses
			// reset in the equalizer settings)
			repeat(equalizer.numberOfBands.toInt()) { band ->
				equalizer.setBandLevel(band.toShort(), 0)
			}
			config.bandLevels.forEach { (band, level) ->
				equalizer.setBandLevel(band.toShort(), level.toInt().toShort())
			}
		} catch (ex: Exception) {
			Logger.e("ExoEqualizerManager", "error while setting eq band levels", ex)
		}
	}

	fun updateConfig() {
		val mode = equalizerManager.config.value.mode
		if (mode != equalizerMode) {
			applyEqualizerMode(mode)
		} else {
			updateEqualizer()
		}
	}

	fun applyEqualizerMode(mode: EqualizerMode) {
		applyEqualizerMode(mode, closeCurrentSession = true)
	}

	private fun applyEqualizerMode(mode: EqualizerMode, closeCurrentSession: Boolean) {
		equalizerMode = mode
		if (closeCurrentSession) {
			releaseEqualizer()
		}

		when (mode) {
			EqualizerMode.BuiltIn -> createEqualizer(currentAudioSessionId)
			EqualizerMode.External -> openAudioEffectSession(currentAudioSessionId)
			EqualizerMode.Disabled -> Unit
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
	private fun closeAudioEffectSession() {
		if (currentAudioSessionId == C.AUDIO_SESSION_ID_UNSET) return
		context.sendBroadcast(
			Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
				putExtra(AudioEffect.EXTRA_AUDIO_SESSION, currentAudioSessionId)
				putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
			}
		)
		currentAudioSessionId = C.AUDIO_SESSION_ID_UNSET
	}

	fun releaseEqualizer() {
		this.equalizer?.release()
		this.equalizer = null
		closeAudioEffectSession()
	}
}
