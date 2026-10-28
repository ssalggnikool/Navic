/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import android.content.Context
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageResult
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import coil3.size.Dimension
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.runtime.StorageManager
import paige.navic.util.network.NavicDnsResolver
import paige.navic.util.network.createHttpClientWithPreferences
import kotlin.math.abs

class CoilSingleton(
	private val context: Context,
	preferenceManager: PreferenceManager,
	dnsResolver: NavicDnsResolver,
	private val storageManager: StorageManager
) {
	private var sharedDiskCache: DiskCache? = null
	private var sharedMemoryCache: MemoryCache? = null

	@OptIn(ExperimentalCoilApi::class)
	private val networkFetcher = KtorNetworkFetcherFactory(
		createHttpClientWithPreferences(preferenceManager, dnsResolver)
	)

	// https://slack-chats.kotlinlang.org/t/8294624/hi-i-m-using-coil-to-load-images-in-compose-and-i-need-to-se
	private inner class SizeInterceptor : Interceptor {
		private val sizes = listOf(50, 100, 200, 512, 1024, 4096)

		override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
			val request = chain.request
			val width = chain.size.width
			val newRequest = if (width is Dimension.Pixels && request.data is String) {
				val url = (request.data as String).replace(
					Regex("[?&]size=[0-9]+"), ""
				)

				val targetSize = sizes.minBy { abs(it - width.px) }.toString()

				val uri = url.toUri()
				val newUri = uri
					.buildUpon()
					.appendQueryParameter("size", targetSize)
					.build()

				val idParam = uri.getQueryParameter("id")
				val baseKey = idParam ?: request.diskCacheKey
				val sizedKey = "#size=$targetSize".let { suffix ->
					idParam?.let { "$it$suffix" } ?: "$url$suffix"
				}

				val diskCache = getDiskCache()
				val (finalData, finalCacheKey) = when {
					diskCache.openSnapshot(sizedKey)?.use { true } == true -> {
						newUri to sizedKey
					}

					baseKey != null && diskCache.openSnapshot(baseKey)?.use { true } == true -> {
						request.data to baseKey
					}

					else -> {
						newUri to sizedKey
					}
				}

				val builder = request.newBuilder().data(finalData)
				request.memoryCacheKey?.let {
					builder.memoryCacheKey(finalCacheKey)
				}
				request.diskCacheKey?.let {
					builder.diskCacheKey(finalCacheKey)
				}
				builder.build()
			} else {
				request
			}
			return chain.withRequest(newRequest).proceed()
		}
	}

	private inner class LocalCoverInterceptor : Interceptor {
		override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
			val request = chain.request
			val coverId = (request.data as? String)
				?.toUri()
				?.getQueryParameter("id")
				?: request.diskCacheKey?.substringBefore("#size=")

			val file = coverId?.let(storageManager::getCoverArtFile)
				?: return chain.proceed()

			return chain.withRequest(
				request.newBuilder()
					.data(file)
					.diskCachePolicy(CachePolicy.DISABLED)
					.build()
			).proceed()
		}
	}

	private val sizeInterceptor = SizeInterceptor()
	private val localCoverInterceptor = LocalCoverInterceptor()

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
				add(localCoverInterceptor)
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
				add(localCoverInterceptor)
				add(sizeInterceptor)
			}
			.diskCache { getDiskCache() }
			.memoryCache { getMemoryCache() }
			.crossfade(true)
			.build()
	}
}
