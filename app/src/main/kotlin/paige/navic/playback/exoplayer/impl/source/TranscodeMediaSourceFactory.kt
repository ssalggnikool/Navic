/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.source

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.CmcdConfiguration
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.text.SubtitleParser
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.connectivity.ConnectivityManager
import paige.navic.domain.manager.session.supportsExtension
import paige.navic.domain.server.ServerExtension
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource

/**
 * uses [TimeOffsetMediaSource] for songs that will be transcoded,
 * and delegates everything else to the normal progressive source
 */
@OptIn(UnstableApi::class, ExperimentalApi::class)
@Suppress("DEPRECATION")
internal class TranscodeMediaSourceFactory(
	private val delegate: MediaSource.Factory,
	private val dataSourceFactory: DataSource.Factory,
	private val extractorsFactory: ExtractorsFactory,
	private val preferenceManager: PreferenceManager,
	private val connectivityManager: ConnectivityManager,
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager
) : MediaSource.Factory {
	override fun createMediaSource(mediaItem: MediaItem): MediaSource {
		val uri = mediaItem.localConfiguration?.uri
		val songId =
			if (uri?.scheme == NavicDataSource.NAVIC_SCHEME && uri.host == NavicDataSource.NAVIC_SONG_HOST) {
				uri.lastPathSegment
			} else {
				null
			}
		val durationMs = mediaItem.mediaMetadata.durationMs
		val downloadable = songId != null && !songId.startsWith(RADIO_PREFIX)
		val downloaded = downloadable && downloadManager.downloadedSongs.value.containsKey(songId)

		return if (
			songId != null &&
			downloadable &&
			!downloaded &&
			durationMs != null &&
			durationMs > 0L &&
			isTranscodingActive() &&
			sessionManager.supportsExtension(ServerExtension.TRANSCODE_OFFSET)
		) {
			TimeOffsetMediaSource(
				mediaItem = mediaItem,
				durationUs = durationMs * 1_000L,
				dataSourceFactory = dataSourceFactory,
				extractorsFactory = extractorsFactory
			)
		} else {
			delegate.createMediaSource(mediaItem)
		}
	}

	override fun getSupportedTypes(): IntArray = delegate.supportedTypes

	override fun setDrmSessionManagerProvider(
		drmSessionManagerProvider: DrmSessionManagerProvider
	): MediaSource.Factory = apply { delegate.setDrmSessionManagerProvider(drmSessionManagerProvider) }

	override fun setLoadErrorHandlingPolicy(
		loadErrorHandlingPolicy: LoadErrorHandlingPolicy
	): MediaSource.Factory = apply { delegate.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy) }

	override fun setCmcdConfigurationFactory(
		cmcdConfigurationFactory: CmcdConfiguration.Factory
	): MediaSource.Factory = apply { delegate.setCmcdConfigurationFactory(cmcdConfigurationFactory) }

	@Deprecated("Deprecated in Java")
	override fun experimentalParseSubtitlesDuringExtraction(
		parseSubtitlesDuringExtraction: Boolean
	): MediaSource.Factory =
		apply { delegate.experimentalParseSubtitlesDuringExtraction(parseSubtitlesDuringExtraction) }

	override fun setSubtitleParserFactory(
		subtitleParserFactory: SubtitleParser.Factory
	): MediaSource.Factory = apply { delegate.setSubtitleParserFactory(subtitleParserFactory) }

	private fun isTranscodingActive(): Boolean {
		val (bitrate, container) = NavicDataSource.getCurrentQuality(preferenceManager, connectivityManager)
		return bitrate > 0 || container != null
	}

	private companion object {
		const val RADIO_PREFIX = "radio_"
	}
}
