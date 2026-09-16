package paige.navic.di

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import okio.FileSystem
import coil3.PlatformContext as CoilPlatformContext

private var sharedDiskCache: DiskCache? = null

private fun getDiskCache(): DiskCache {
	return sharedDiskCache ?: DiskCache.Builder()
		.directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
		.maxSizeBytes(2L shl 30)
		.build().also { sharedDiskCache = it }
}

fun initializeSingletonImageLoader(context: CoilPlatformContext): ImageLoader {
	return ImageLoader.Builder(context)
		.components {
			add(KtorNetworkFetcherFactory())
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
fun getStaticImageLoader(context: CoilPlatformContext): ImageLoader {
	return ImageLoader.Builder(context)
		.serviceLoaderEnabled(false)
		.components {
			add(KtorNetworkFetcherFactory())
		}
		.diskCache { getDiskCache() }
		.crossfade(true)
		.build()
}
