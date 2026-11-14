/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.source

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.StreamKey
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.LoadingInfo
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.BundledExtractorsAdapter
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSource.MediaPeriodId
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SampleStream
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.extractor.ExtractorsFactory

/**
 * a single-period source that presents a Subsonic transcoded stream as a normal seekable item
 *
 * this source reports a synthetic timeline with the duration from [MediaItem.mediaMetadata],
 * and turns every seek into a new `timeOffset` request for the server
 */
@OptIn(UnstableApi::class)
internal class TimeOffsetMediaSource(
	private val mediaItem: MediaItem,
	private val durationUs: Long,
	private val dataSourceFactory: DataSource.Factory,
	private val extractorsFactory: ExtractorsFactory
) : BaseMediaSource() {
	private val timeline =
		SinglePeriodTimeline(
			/* durationUs= */ durationUs,
			/* isSeekable= */ true,
			/* isDynamic= */ false,
			/* useLiveConfiguration= */ false,
			/* manifest= */ null,
			mediaItem
		)

	override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
		refreshSourceInfo(timeline)
	}

	override fun maybeThrowSourceInfoRefreshError() = Unit

	override fun releaseSourceInternal() = Unit

	override fun getMediaItem(): MediaItem = mediaItem

	override fun createPeriod(id: MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod =
		TimeOffsetMediaPeriod(
			mediaItem = mediaItem,
			durationUs = durationUs,
			dataSourceFactory = dataSourceFactory,
			extractorsFactory = extractorsFactory,
			allocator = allocator,
			playerId = playerId,
			bandwidthMeter = bandwidthMeter
		)

	override fun releasePeriod(mediaPeriod: MediaPeriod) {
		(mediaPeriod as TimeOffsetMediaPeriod).release()
	}
}

@OptIn(UnstableApi::class, ExperimentalApi::class)
private class TimeOffsetMediaPeriod(
	private val mediaItem: MediaItem,
	private val durationUs: Long,
	private val dataSourceFactory: DataSource.Factory,
	private val extractorsFactory: ExtractorsFactory,
	private val allocator: Allocator,
	private val playerId: PlayerId,
	private val bandwidthMeter: BandwidthMeter
) : MediaPeriod {

	private val state = TranscodeStreamState(durationUs)
	private val childCaller =
		MediaSource.MediaSourceCaller { _, _ -> }

	private val childCallback =
		object : MediaPeriod.Callback {
			override fun onPrepared(mediaPeriod: MediaPeriod) {
				if (!prepared) {
					val pendingSeek = pendingSeekUs
					pendingSeekUs = C.TIME_UNSET
					if (pendingSeek != C.TIME_UNSET) {
						state.seek(pendingSeek)
						childPeriod?.seekToUs(0L)
					}
					prepared = true
					callback?.onPrepared(this@TimeOffsetMediaPeriod)
				}
			}

			override fun onContinueLoadingRequested(source: MediaPeriod) {
				callback?.onContinueLoadingRequested(this@TimeOffsetMediaPeriod)
			}
		}

	private var callback: MediaPeriod.Callback? = null
	private var childSource: MediaSource? = null
	private var childPeriod: MediaPeriod? = null
	private var prepared = false
	private var pendingSeekUs = C.TIME_UNSET

	override fun prepare(callback: MediaPeriod.Callback, positionUs: Long) {
		this.callback = callback
		state.seek(positionUs)
		prepareChild()
	}

	override fun maybeThrowPrepareError() {
		childPeriod?.maybeThrowPrepareError()
	}

	override fun getStreamKeys(trackSelections: List<ExoTrackSelection>): List<StreamKey> =
		if (prepared) childPeriod?.getStreamKeys(trackSelections).orEmpty() else emptyList()

	override fun getTrackGroups(): TrackGroupArray = checkNotNull(childPeriod).trackGroups

	override fun selectTracks(
		selections: Array<ExoTrackSelection?>,
		mayRetainStreamFlags: BooleanArray,
		streams: Array<SampleStream?>,
		streamResetFlags: BooleanArray,
		positionUs: Long
	): Long =
		checkNotNull(childPeriod)
			.selectTracks(
				selections,
				mayRetainStreamFlags,
				streams,
				streamResetFlags,
				positionUs
			)

	override fun discardBuffer(positionUs: Long, toKeyframe: Boolean) {
		if (prepared) {
			childPeriod?.discardBuffer(positionUs, toKeyframe)
		}
	}

	override fun reevaluateBuffer(positionUs: Long) {
		updateResumePosition(positionUs)
		childPeriod?.reevaluateBuffer(positionUs)
	}

	override fun readDiscontinuity(): Long {
		val childDiscontinuity = childPeriod?.readDiscontinuity() ?: C.TIME_UNSET

		return if (childDiscontinuity != C.TIME_UNSET) state.offsetUs else C.TIME_UNSET
	}

	override fun getAdjustedSeekPositionUs(positionUs: Long, seekParameters: SeekParameters): Long =
		positionUs.coerceIn(0L, durationUs)

	override fun seekToUs(positionUs: Long): Long {
		val target = positionUs.coerceIn(0L, durationUs)
		state.seek(target)
		if (prepared) {
			childPeriod?.seekToUs(0L)
		} else {
			pendingSeekUs = target
		}
		return target
	}

	override fun getBufferedPositionUs(): Long =
		if (prepared) {
			floorAtStreamStart(childPeriod?.getBufferedPositionUs())
		} else {
			C.TIME_UNSET
		}

	override fun getNextLoadPositionUs(): Long {
		if (!prepared) {
			return C.TIME_UNSET
		}
		val position = floorAtStreamStart(childPeriod?.getNextLoadPositionUs())
		if (position != C.TIME_UNSET && position != C.TIME_END_OF_SOURCE) {
			state.nextLoadPositionUs = position
		}
		return position
	}

	override fun continueLoading(loadingInfo: LoadingInfo): Boolean {
		updateResumePosition(loadingInfo.playbackPositionUs)
		val result = childPeriod?.continueLoading(loadingInfo) ?: false
		if (prepared) {
			val next = childPeriod?.getNextLoadPositionUs() ?: C.TIME_UNSET
			if (next != C.TIME_UNSET && next != C.TIME_END_OF_SOURCE) {
				state.nextLoadPositionUs = next
			}
		}
		return result
	}

	override fun isLoading(): Boolean = childPeriod?.isLoading() ?: false

	override fun setUsesStreamPrerollFlags() {
		childPeriod?.setUsesStreamPrerollFlags()
	}

	override fun setEndPositionUs(endPositionUs: Long): Long =
		childPeriod?.setEndPositionUs(endPositionUs) ?: C.TIME_END_OF_SOURCE

	fun release() {
		prepared = false
		callback = null
		val period = childPeriod
		val source = childSource
		childPeriod = null
		childSource = null
		if (period != null && source != null) {
			source.releasePeriod(period)
			source.releaseSource(childCaller)
		}
	}

	private fun prepareChild() {
		val childFactory =
			ProgressiveMediaSource.Factory(
				TranscodeDataSourceFactory(dataSourceFactory, state)
			) { _ ->
				OffsettingProgressiveMediaExtractor(
					BundledExtractorsAdapter(extractorsFactory),
					state
				)
			}
		val source = childFactory.createMediaSource(mediaItem)
		source.prepareSource(childCaller, playerId, bandwidthMeter)
		val period = source.createPeriod(MediaPeriodId(Any()), allocator, /* startPositionUs= */ 0L)
		period.prepare(childCallback, /* positionUs= */ 0L)
		childSource = source
		childPeriod = period
	}

	private fun floorAtStreamStart(position: Long?): Long {
		if (position == null || position == C.TIME_UNSET || position == C.TIME_END_OF_SOURCE) {
			return position ?: C.TIME_UNSET
		}
		return maxOf(position, state.offsetUs).coerceAtMost(durationUs)
	}

	private fun updateResumePosition(positionUs: Long) {
		if (positionUs != C.TIME_UNSET && positionUs > state.resumePositionUs) {
			state.resumePositionUs = positionUs
		}
	}
}
