/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.cache

import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dev.zt64.subsonic.api.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.StreamCacheManager
import paige.navic.playback.PlaybackUtils
import paige.navic.util.network.NavicularDnsResolver
import paige.navic.util.network.createOkHttpClient
import java.io.File

@OptIn(UnstableApi::class)
class NavicDataSource(
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager,
	private val streamCacheManager: StreamCacheManager,
	preferenceManager: PreferenceManager,
	dnsResolver: NavicularDnsResolver,
) : BaseDataSource(true) {
	class NavicDataSourceFactory(
		private val downloadManager: DownloadManager,
		private val sessionManager: SessionManager,
		private val streamCacheManager: StreamCacheManager,
		private val preferenceManager: PreferenceManager,
		private val dnsResolver: NavicularDnsResolver,
	) : DataSource.Factory {
		override fun createDataSource(): DataSource {
			return NavicDataSource(
				downloadManager,
				sessionManager,
				streamCacheManager,
				preferenceManager,
				dnsResolver
			)
		}
	}

	companion object {
		const val NAVIC_SCHEME = "navic"
		const val NAVIC_HOST = "song"

		fun String.buildSongUri(): Uri {
			return "$NAVIC_SCHEME://$NAVIC_HOST/$this".toUri()
		}
	}

	private val clientInfo = PlaybackUtils.buildClientInfo()

	// TODO: figure out why KtorDataSource causes #650
	private val upstreamFactory = OkHttpDataSource.Factory(
		createOkHttpClient(
			preferenceManager.dangerousSslNoopEnabled,
			preferenceManager.customHeadersMap(),
			preferenceManager.proxyUrl.takeIf { it.isNotEmpty() },
			dnsResolver
		)
	)

	private val cacheDataSourceFactory by lazy {
		CacheDataSource.Factory()
			.setCache(streamCacheManager.cache)
			.setUpstreamDataSourceFactory(upstreamFactory)
			.setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
	}

	private var wrappedDataSource: DataSource? = null
	private var currentUri: Uri? = null

	override fun open(dataSpec: DataSpec): Long {
		transferInitializing(dataSpec)
		val uri = dataSpec.uri
		currentUri = uri

		val songId = if (uri.scheme == NAVIC_SCHEME && uri.host == NAVIC_HOST) {
			uri.lastPathSegment
		} else {
			null
		}

		var cacheKey: String? = null

		val targetUri = runBlocking {
			withContext(Dispatchers.IO) {
				if (songId != null) {
					val localPath = downloadManager.getDownloadedFilePath(songId)
					if (localPath != null) {
						Uri.fromFile(File(localPath))
					} else {
						cacheKey = streamCacheManager.keyFor(songId)
						resolveStreamUrl(songId)
					}
				} else {
					uri
				}
			}
		}

		val resolvedDataSpecBuilder = dataSpec.buildUpon().setUri(targetUri)
		if (cacheKey != null) {
			resolvedDataSpecBuilder
				.setKey(cacheKey)
				.setFlags(dataSpec.flags and DataSpec.FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN.inv())
		}
		val resolvedDataSpec = resolvedDataSpecBuilder.build()

		val source = when {
			targetUri.scheme == "file" || targetUri.scheme == "content" -> FileDataSource()
			cacheKey != null -> cacheDataSourceFactory.createDataSource()
			else -> upstreamFactory.createDataSource()
		}

		wrappedDataSource = source as DataSource?
		val length = source.open(resolvedDataSpec)
		transferStarted(dataSpec)
		return length
	}

	override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
		val bytesRead = wrappedDataSource?.read(buffer, offset, length) ?: C.RESULT_END_OF_INPUT
		if (bytesRead > 0) {
			bytesTransferred(bytesRead)
		}
		return bytesRead
	}

	override fun getUri(): Uri? {
		return wrappedDataSource?.uri ?: currentUri
	}

	override fun close() {
		wrappedDataSource?.close()
		wrappedDataSource = null
		currentUri = null
		transferEnded()
	}

	// TODO: make transcoding options work again
	private suspend fun resolveStreamUrl(id: String): Uri {
		val defaultStreamUrl = sessionManager.api.getStreamUrl(id, 0, estimateContentLength = true)
		return try {
			val transcodeDecision = withContext(Dispatchers.IO) {
				sessionManager.api.getTranscodeDecision(id, MediaType.SONG, clientInfo)
			}
			val streamUrlStr =
				if (!transcodeDecision.canDirectPlay && transcodeDecision.canTranscode && transcodeDecision.transcodeParams != null) {
					sessionManager.api.getTranscodeStreamUrl(
						id, MediaType.SONG, 0,
						transcodeDecision.transcodeParams!!
					)
				} else {
					defaultStreamUrl
				}
			streamUrlStr.toUri()
		} catch (_: Exception) {
			defaultStreamUrl.toUri()
		}
	}
}
