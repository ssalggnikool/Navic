/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.source

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.DataReader
import androidx.media3.common.Format
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.source.ProgressiveMediaExtractor
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput

/**
 * every stream the server hands us starts at some [offsetUs] and its timestamps restart near zero
 * [nextOpenOffset] decides where the next `open()` should start, and the extractor output shifts
 * every sample timestamp so the sample queue always sees one continuous absolute timeline
 */
@OptIn(UnstableApi::class)
internal class TranscodeStreamState(private val durationUs: Long) {
	@Volatile
	internal var generation: Long = 0L

	// absolute time of the first sample of the current stream
	@Volatile
	internal var offsetUs: Long = 0L

	// playback position as last reported by the player
	@Volatile
	internal var resumePositionUs: Long = 0L

	@Volatile
	internal var nextLoadPositionUs: Long = 0L

	private var seekPending = true
	private var seekTargetUs = 0L

	@Synchronized
	internal fun seek(positionUs: Long) {
		val target = positionUs.coerceIn(0L, durationUs)
		seekPending = true
		seekTargetUs = target
		offsetUs = target
		resumePositionUs = target
	}

	@Synchronized
	internal fun nextOpenOffset(requestedPosition: Long): Long {
		val wasSeek = seekPending
		val chosen =
			when {
				wasSeek -> {
					seekPending = false
					seekTargetUs
				}
				requestedPosition > 0L && nextLoadPositionUs > 0L -> nextLoadPositionUs
				// resume where playback actually is
				else -> resumePositionUs
			}.coerceIn(0L, durationUs)
		offsetUs = chosen
		generation++
		return chosen
	}
}

@OptIn(UnstableApi::class)
internal class TranscodeDataSourceFactory(
	private val upstreamFactory: DataSource.Factory,
	private val state: TranscodeStreamState
) : DataSource.Factory {

	override fun createDataSource(): DataSource =
		TranscodeDataSource(
			upstreamFactory.createDataSource(),
			state
		)
}

/**
 * translates ExoPlayer's byte-space reads into Subsonic `timeOffset` requests
 *
 * the server sends a transcoded stream with no length and no duration,
 * which makes ExoPlayer treat it as live and unseekable.
 *
 * ...unless if we request the stream with `estimateContentLength`,
 * which fakes a length to us and has a myriad of issues
 */
@OptIn(UnstableApi::class)
private class TranscodeDataSource(
	private val upstream: DataSource,
	private val state: TranscodeStreamState
) : BaseDataSource(/* isNetwork= */ true) {

	private var currentUri: Uri? = null
	private var opened = false

	override fun open(dataSpec: DataSpec): Long {
		val offsetUs = state.nextOpenOffset(dataSpec.position)
		val seconds = (offsetUs / 1_000_000L).toInt()
		val uri =
			dataSpec.uri
				.buildUpon()
				.appendQueryParameter(PARAM_TRANSCODE, "1")
				.appendQueryParameter(PARAM_TIME_OFFSET, seconds.toString())
				.build()
		val request =
			dataSpec
				.buildUpon()
				.setUri(uri)
				.setPosition(0L)
				.setLength(C.LENGTH_UNSET.toLong())
				.build()

		transferInitializing(request)
		upstream.open(request)
		opened = true
		currentUri = uri
		transferStarted(request)

		// override what extractors would return here with unset
		return C.LENGTH_UNSET.toLong()
	}

	override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
		val bytesRead = upstream.read(buffer, offset, length)
		if (bytesRead > 0) {
			bytesTransferred(bytesRead)
		}
		return bytesRead
	}

	override fun getUri(): Uri? = currentUri ?: upstream.uri

	override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

	override fun close() {
		if (opened) {
			opened = false
			transferEnded()
		}
		upstream.close()
		currentUri = null
	}

	companion object {
		const val PARAM_TRANSCODE = "transcode"
		const val PARAM_TIME_OFFSET = "timeOffset"
	}
}

@OptIn(UnstableApi::class)
internal class OffsettingProgressiveMediaExtractor(
	private val delegate: ProgressiveMediaExtractor,
	private val state: TranscodeStreamState
) : ProgressiveMediaExtractor {
	private var wrappedOutput: OffsettingExtractorOutput? = null

	override fun init(
		dataReader: DataReader,
		uri: Uri,
		responseHeaders: Map<String, List<String>>,
		position: Long,
		length: Long,
		output: ExtractorOutput
	) {
		val wrapped = wrappedOutput ?: OffsettingExtractorOutput(output, state).also { wrappedOutput = it }
		delegate.init(dataReader, uri, responseHeaders, position, length, wrapped)
	}

	override fun release() = delegate.release()

	override fun disableSeekingOnMp3Streams() = delegate.disableSeekingOnMp3Streams()

	override fun getCurrentInputPosition(): Long = delegate.currentInputPosition

	override fun seek(position: Long, seekTimeUs: Long) = delegate.seek(position, seekTimeUs)

	override fun read(positionHolder: PositionHolder): Int = delegate.read(positionHolder)
}

@OptIn(UnstableApi::class)
private class OffsettingExtractorOutput(
	private val delegate: ExtractorOutput,
	private val state: TranscodeStreamState
) : ExtractorOutput {
	private val trackOutputs = HashMap<Int, OffsettingTrackOutput>()

	override fun track(id: Int, type: Int): TrackOutput =
		trackOutputs.getOrPut(id) { OffsettingTrackOutput(delegate.track(id, type), state) }

	override fun endTracks() = delegate.endTracks()

	override fun seekMap(seekMap: SeekMap) {
		delegate.seekMap(SeekMap.Unseekable(C.TIME_UNSET))
	}
}

@OptIn(UnstableApi::class)
private class OffsettingTrackOutput(
	private val delegate: TrackOutput,
	private val state: TranscodeStreamState
) : TrackOutput {
	private var mappedGeneration = Long.MIN_VALUE
	private var firstSampleTimeUs = C.TIME_UNSET

	override fun format(format: Format) = delegate.format(format)

	override fun durationUs(durationUs: Long) = delegate.durationUs(durationUs)

	override fun sampleData(
		input: DataReader,
		length: Int,
		allowEndOfInput: Boolean,
		sampleDataPart: Int
	): Int = delegate.sampleData(input, length, allowEndOfInput, sampleDataPart)

	override fun sampleData(data: ParsableByteArray, length: Int, sampleDataPart: Int) =
		delegate.sampleData(data, length, sampleDataPart)

	override fun sampleMetadata(
		timeUs: Long,
		flags: Int,
		size: Int,
		offset: Int,
		cryptoData: TrackOutput.CryptoData?
	) = delegate.sampleMetadata(mapTimestamp(timeUs), flags, size, offset, cryptoData)

	private fun mapTimestamp(timeUs: Long): Long {
		if (timeUs == C.TIME_UNSET) {
			return timeUs
		}
		val generation = state.generation
		if (generation != mappedGeneration) {
			mappedGeneration = generation
			firstSampleTimeUs = timeUs
		}
		return if (firstSampleTimeUs == C.TIME_UNSET) {
			timeUs
		} else {
			state.offsetUs + (timeUs - firstSampleTimeUs)
		}
	}
}
