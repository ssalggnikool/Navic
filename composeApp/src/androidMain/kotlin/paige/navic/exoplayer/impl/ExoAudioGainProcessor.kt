package paige.navic.exoplayer.impl

import android.media.AudioFormat
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
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
		if (inputAudioFormat.encoding == AudioFormat.ENCODING_PCM_16BIT) {
			return inputAudioFormat
		}
		throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
	}

	override fun queueInput(inputBuffer: ByteBuffer) {
		val pos = inputBuffer.position()
		val limit = inputBuffer.limit()
		val outputBuffer = replaceOutputBuffer(limit - pos)

		inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
		outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

		val computedVolume = finalVolume

		if (abs(computedVolume - 1.0f) < 0.0001f) {
			outputBuffer.put(inputBuffer)
		} else {
			val shortBufferInput = inputBuffer.asShortBuffer()
			val shortBufferOutput = outputBuffer.asShortBuffer()

			while (shortBufferInput.hasRemaining()) {
				// prevent popping
				val sample = shortBufferInput.get()
				val scaledSample = (sample * computedVolume)
					.toInt()
					.coerceAtLeast(Short.MIN_VALUE.toInt())
					.coerceAtMost(Short.MAX_VALUE.toInt())
					.toShort()

				shortBufferOutput.put(scaledSample)
			}
			inputBuffer.position(inputBuffer.position() + shortBufferInput.position() * 2)
			outputBuffer.position(outputBuffer.position() + shortBufferOutput.position() * 2)
		}
		outputBuffer.flip()
	}
}
