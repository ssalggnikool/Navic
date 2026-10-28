/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import paige.navic.discord.uploader.FreeImageHostUploader
import paige.navic.discord.uploader.ImgBbUploader
import paige.navic.discord.uploader.catbox.CatboxUploader
import paige.navic.discord.uploader.catbox.LitterboxUploader
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.model.settings.CoverArtHost
import paige.navic.util.Logger
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.createHttpClient
import kotlin.time.Duration.Companion.seconds

class CoverArtUploadManager(
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager,
	private val settings: Settings,
	dnsResolver: NavicDnsResolver
) {
	private val client = createHttpClient(
		trustAllCerts = preferenceManager.dangerousSslNoopEnabled,
		proxyUrl = preferenceManager.proxyUrl,
		dnsResolver = dnsResolver
	)
	private val mutex = Mutex()
	private val json = Json { ignoreUnknownKeys = true }

	private companion object {
		const val TAG = "CoverArtUpload"
		const val CACHE_KEY = "discordCoverArtCache"
		const val CACHE_LIMIT = 200
		const val TTL_MS = 1L * 60 * 60 * 1000
	}

	@Serializable
	private data class CachedCover(
		val url: String,
		val expiresAt: Long? = null
	)

	suspend fun getPublicUrl(coverArtId: String): String? = mutex.withLock {
		for ((host, enabled) in preferenceManager.discordCoverArtProviders.providers) {
			if (!enabled) continue

			val cacheKey = "${host.name}:$coverArtId"
			cached(cacheKey)?.let { return@withLock it }

			val uploader = uploaderFor(host) ?: continue
			val url = try {
				val bytes = sessionManager.api.getCoverArt(coverArtId, size = "512")
				val image = bytes.toCoverArtImage()
					?: error("cover art is not an image")
				withTimeoutOrNull(15.seconds) { uploader.upload(image) }
					?: error("upload timed out")
			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Logger.w(TAG, "$host upload failed for $coverArtId", e)
				continue
			}

			cache(cacheKey, url)
			return@withLock url
		}

		null
	}

	private fun uploaderFor(host: CoverArtHost): CoverArtUploader? = when (host) {
		CoverArtHost.Catbox -> CatboxUploader(client)
		CoverArtHost.Litterbox -> LitterboxUploader(client)
		CoverArtHost.FreeImageHost -> FreeImageHostUploader(client)
		CoverArtHost.ImgBB -> preferenceManager.discordCoverArtApiKeyProviders[host.name]
			?.takeIf { it.isNotBlank() }
			?.let { ImgBbUploader(client, it) }
	}

	private fun cached(key: String): String? {
		val cache = readCache()
		val entry = cache[key] ?: return null
		val expiresAt = entry.expiresAt
		if (expiresAt != null && expiresAt <= System.currentTimeMillis()) {
			cache.remove(key)
			writeCache(cache)
			return null
		}
		return entry.url
	}

	private fun cache(key: String, url: String) {
		val cache = readCache()
		if (cache.size >= CACHE_LIMIT && key !in cache) cache.clear()
		cache[key] = CachedCover(
			url = url,
			expiresAt = System.currentTimeMillis() + TTL_MS
		)
		writeCache(cache)
	}

	private fun readCache(): MutableMap<String, CachedCover> {
		val raw = settings.getStringOrNull(CACHE_KEY) ?: return mutableMapOf()
		return try {
			json.decodeFromString<Map<String, CachedCover>>(raw).toMutableMap()
		} catch (e: Exception) {
			Logger.w(TAG, "could not read cover art cache", e)
			mutableMapOf()
		}
	}

	private fun writeCache(cache: Map<String, CachedCover>) {
		settings.putString(CACHE_KEY, json.encodeToString(cache))
	}
}
