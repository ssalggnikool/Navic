package paige.navic.di

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import okio.FileSystem
import paige.navic.domain.manager.PreferenceManager
import paige.navic.util.createHttpClientWithPreferences
import coil3.PlatformContext as CoilPlatformContext


class CoilSingleton(
	private val preferenceManager: PreferenceManager,
	private val context: CoilPlatformContext
) {
	private var sharedDiskCache: DiskCache? = null

	val coilImageLoader: ImageLoader by lazy { getImageLoader() }
	val staticCoilImageLoader: ImageLoader by lazy { getStaticImageLoader() }

	private fun getDiskCache(): DiskCache {
		return sharedDiskCache ?: DiskCache.Builder()
			.directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
			.maxSizeBytes(2L shl 30)
			.build().also { sharedDiskCache = it }
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
			.crossfade(true)
			.build()
	}
}

