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
import paige.navic.preferences.PreferenceManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.connectivity.ConnectivityManager
import paige.navic.domain.manager.session.supportsExtension
import paige.navic.domain.server.ServerExtension
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource

/**
 * this uses [TimeOffsetMediaSource] for OpenSubsonic transcode streams
 * (so that we can abuse the timeOffset extension for allowing proper seeking),
 * and delegates everything else to the normal progressive/HLS sources
 */
@OptIn(UnstableApi::class, ExperimentalApi::class)
@Suppress("DEPRECATION")
internal class NavicMediaSourceFactory(
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
		val (bitrateKbps, format) =
			NavicDataSource.getCurrentQuality(preferenceManager, connectivityManager)

		// hls playlist base uris are relative, so exoplayer needs the real url as the item uri
		val hlsUrl = songId
			?.takeIf { downloadable && !downloaded }
			?.let { sessionManager.api.getHlsStreamUrl(it, bitrateKbps, format) }

		return when {
			hlsUrl != null -> delegate.createMediaSource(
				mediaItem.buildUpon().setUri(hlsUrl).build()
			)

			songId != null &&
				downloadable &&
				!downloaded &&
				durationMs != null &&
				durationMs > 0L &&
				(bitrateKbps > 0 || format != null) &&
				sessionManager.supportsExtension(ServerExtension.OPENSUBSONIC_TRANSCODE_OFFSET) ->
				TimeOffsetMediaSource(
					mediaItem = mediaItem,
					durationUs = durationMs * 1_000L,
					dataSourceFactory = dataSourceFactory,
					extractorsFactory = extractorsFactory
				)

			else -> delegate.createMediaSource(mediaItem)
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

	private companion object {
		const val RADIO_PREFIX = "radio_"
	}
}
