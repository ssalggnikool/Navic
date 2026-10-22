/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import paige.navic.util.xxHash32
import java.io.File

@OptIn(UnstableApi::class)
class StreamCacheManager(
	private val context: Context,
	private val sessionManager: SessionManager
) {
	private companion object {
		const val MAX_CACHE_BYTES = (1024L * 5) * 1024 * 1024
		const val CACHE_KEY_VERSION = "v1"
	}

	private val cacheDirectory = File(context.cacheDir, "stream_cache")
	private val cacheInstance by lazy {
		SimpleCache(
			cacheDirectory,
			LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
			StandaloneDatabaseProvider(context)
		)
	}

	val cache: SimpleCache
		get() = cacheInstance

	val size: Long
		get() = cache.cacheSpace


	fun keyFor(songId: String): String {
		val server = "${sessionManager.instanceUrl}:${sessionManager.username}"

		return "$CACHE_KEY_VERSION/${server.xxHash32()}/$songId"
	}

	fun clear() {
		cache.keys.toList().forEach(cache::removeResource)
	}
}
