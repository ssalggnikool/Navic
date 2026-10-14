/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import android.content.Context
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import paige.navic.domain.manager.PreferenceManager
import paige.navic.util.createHttpClientWithPreferences

class CoilSingleton(
	private val context: Context,
	private val preferenceManager: PreferenceManager
) {
	private var sharedDiskCache: DiskCache? = null
	private var sharedMemoryCache: MemoryCache? = null

	val coilImageLoader: ImageLoader by lazy { getImageLoader() }
	val staticCoilImageLoader: ImageLoader by lazy { getStaticImageLoader() }

	private fun getDiskCache(): DiskCache {
		return sharedDiskCache ?: DiskCache.Builder()
			.directory(context.cacheDir.resolve("image_cache"))
			.maxSizeBytes(2L shl 30)
			.build().also { sharedDiskCache = it }
	}

	private fun getMemoryCache(): MemoryCache {
		return sharedMemoryCache ?: MemoryCache.Builder()
			.maxSizePercent(context, 0.15)
			.build().also { sharedMemoryCache = it }
	}

	@OptIn(ExperimentalCoilApi::class)
	private fun getImageLoader(): ImageLoader {
		return ImageLoader.Builder(context)
			.components {
				add(
					KtorNetworkFetcherFactory(
						createHttpClientWithPreferences(preferenceManager)
					)
				)
			}
			.diskCache { getDiskCache() }
			.memoryCache { getMemoryCache() }
			.crossfade(true)
			.build()
	}

	/**
	 * image loader which doesn't animate GIFs
	 *
	 * only used in `BlendBackground.kt` right now
	 */
	@OptIn(ExperimentalCoilApi::class)
	private fun getStaticImageLoader(): ImageLoader {
		return ImageLoader.Builder(context)
			.serviceLoaderEnabled(false)
			.components {
				add(
					KtorNetworkFetcherFactory(
						createHttpClientWithPreferences(preferenceManager)
					)
				)
			}
			.diskCache { getDiskCache() }
			.memoryCache { getMemoryCache() }
			.crossfade(true)
			.build()
	}
}
