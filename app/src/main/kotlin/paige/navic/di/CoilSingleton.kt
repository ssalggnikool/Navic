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
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageResult
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import coil3.size.Dimension
import paige.navic.domain.manager.PreferenceManager
import paige.navic.util.network.NavicularDnsResolver
import paige.navic.util.network.createHttpClientWithPreferences
import androidx.core.net.toUri
import kotlin.math.abs

class CoilSingleton(
	private val context: Context,
	preferenceManager: PreferenceManager,
	dnsResolver: NavicularDnsResolver
) {
	private var sharedDiskCache: DiskCache? = null
	private var sharedMemoryCache: MemoryCache? = null
	@OptIn(ExperimentalCoilApi::class)
	private val networkFetcher = KtorNetworkFetcherFactory(
		createHttpClientWithPreferences(preferenceManager, dnsResolver)
	)
	// https://slack-chats.kotlinlang.org/t/8294624/hi-i-m-using-coil-to-load-images-in-compose-and-i-need-to-se
	private class SizeInterceptor : Interceptor {
		private val sizes = listOf(50, 100, 200, 512, 1024, 4096)

		override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
			val request = chain.request
			val width = chain.size.width
			val newRequest = if (width is Dimension.Pixels) {
				val url = (request.data as String).replace(
					Regex("[?&]size=[0-9]+"), ""
				)

				val targetSize = sizes.minBy { abs(it - width.px) }.toString()

				val newUri = url.toUri()
					.buildUpon()
					.appendQueryParameter("size", targetSize)
					.build()

				val cacheKey = "$url#size=$targetSize"

				request.newBuilder()
					.data(newUri)
					.memoryCacheKey(cacheKey)
					.diskCacheKey(cacheKey)
					.build()
			} else {
				request
			}
			return chain.withRequest(newRequest).proceed()
		}
	}
	private val sizeInterceptor = SizeInterceptor()

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

	private fun getImageLoader(): ImageLoader {
		return ImageLoader.Builder(context)
			.components {
				add(networkFetcher)
				add(sizeInterceptor)
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
	private fun getStaticImageLoader(): ImageLoader {
		return ImageLoader.Builder(context)
			.serviceLoaderEnabled(false)
			.components {
				add(networkFetcher)
				add(sizeInterceptor)
			}
			.diskCache { getDiskCache() }
			.memoryCache { getMemoryCache() }
			.crossfade(true)
			.build()
	}
}
