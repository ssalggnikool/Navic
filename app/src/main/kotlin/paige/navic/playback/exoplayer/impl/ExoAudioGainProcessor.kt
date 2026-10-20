/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.util.decibelsToLinear
import paige.navic.util.effectiveGain
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

@UnstableApi
class ExoAudioGainProcessor : BaseAudioProcessor() {
	private companion object {
		const val DEFAULT_GAIN_DB = 0f
	}

	private var replayGainMetadata: DomainReplayGain? = null
	private var isReplayGainActive = false

	private val finalVolume: Float
		get() = (if (isReplayGainActive) volume + rgAmpValue else ampValue).decibelsToLinear()

	// we should ONLY flush if the gain has changed, flushing needlessly will cause some "bits" of the music to skip
	// flushing the stream is needed because otherwise the user might hear some crackling after changing values
	private var volume = DEFAULT_GAIN_DB
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	var rgAmpValue = 0f
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	var ampValue = 0f
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	fun applyGainMode(mode: ReplayGainMode) {
		if (mode == ReplayGainMode.Off) {
			resetGain()
			return
		}
		val gain = replayGainMetadata?.effectiveGain(mode)
		if (gain != null) {
			isReplayGainActive = true
			volume = gain
		} else {
			isReplayGainActive = false
			volume = DEFAULT_GAIN_DB
		}
	}

	fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		replayGainMetadata = metadata
		if (metadata == null) {
			isReplayGainActive = false
			volume = DEFAULT_GAIN_DB
		}
	}

	fun resetGain() {
		replayGainMetadata = null
		isReplayGainActive = false
		volume = DEFAULT_GAIN_DB
	}

	override fun isActive(): Boolean {
		return super.isActive() && (abs(finalVolume - 1.0f) > 0.0001f)
	}

	override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
		if (inputAudioFormat.encoding == C.ENCODING_PCM_16BIT ||
			inputAudioFormat.encoding == C.ENCODING_PCM_32BIT ||
			inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT) {
			return inputAudioFormat
		}
		throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
	}

	override fun queueInput(inputBuffer: ByteBuffer) {
		val limit = inputBuffer.limit()
		val outputBuffer = replaceOutputBuffer(limit - inputBuffer.position())

		inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
		outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

		val computedVolume = finalVolume

		if (abs(computedVolume - 1.0f) < 0.0001f) {
			outputBuffer.put(inputBuffer)
			outputBuffer.flip()
			return
		}

		when (inputAudioFormat.encoding) {
			C.ENCODING_PCM_16BIT -> {
				val shortInput = inputBuffer.asShortBuffer()
				val shortOutput = outputBuffer.asShortBuffer()
				while (shortInput.hasRemaining()) {
					val sample = shortInput.get()
					val scaled = (sample * computedVolume)
						.toInt()
						.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
						.toShort()
					shortOutput.put(scaled)
				}
				inputBuffer.position(inputBuffer.position() + shortInput.position() * 2)
				outputBuffer.position(outputBuffer.position() + shortOutput.position() * 2)
			}
			C.ENCODING_PCM_32BIT -> {
				val intInput = inputBuffer.asIntBuffer()
				val intOutput = outputBuffer.asIntBuffer()
				while (intInput.hasRemaining()) {
					val sample = intInput.get()
					val scaled = (sample * computedVolume)
						.toLong()
						.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
						.toInt()
					intOutput.put(scaled)
				}
				inputBuffer.position(inputBuffer.position() + intInput.position() * 4)
				outputBuffer.position(outputBuffer.position() + intOutput.position() * 4)
			}
			C.ENCODING_PCM_FLOAT -> {
				val floatInput = inputBuffer.asFloatBuffer()
				val floatOutput = outputBuffer.asFloatBuffer()
				while (floatInput.hasRemaining()) {
					val sample = floatInput.get()
					val scaled = sample * computedVolume
					floatOutput.put(scaled)
				}
				inputBuffer.position(inputBuffer.position() + floatInput.position() * 4)
				outputBuffer.position(outputBuffer.position() + floatOutput.position() * 4)
			}
			else -> {
				throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
			}
		}
		outputBuffer.flip()
	}
}
