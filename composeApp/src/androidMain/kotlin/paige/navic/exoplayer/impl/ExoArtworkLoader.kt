package paige.navic.exoplayer.impl

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import coil3.ImageLoader
import com.google.common.util.concurrent.ListenableFuture
import paige.navic.util.Logger

/**
 * Small util for attempting to load cached artworks from Coil first,
 * before resorting to doing the normal behavior of ExoPlayer (which just fetches the provided image url)
 */
@UnstableApi
class ExoArtworkLoader(
	context: Context,
	private val imageLoader: ImageLoader,
): BitmapLoader {
	private val bitmapLoader = DataSourceBitmapLoader.Builder(context).build()

	override fun supportsMimeType(mimeType: String): Boolean {
		return bitmapLoader.supportsMimeType(mimeType)
	}

	override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
		return bitmapLoader.decodeBitmap(data)
	}

	override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
		// this expects to be the coverArtId from Subsonic's API, since currently the cache is keyed against it
		val coverId = uri.getQueryParameter("id")

		if (coverId != null) {
			try {
				val diskCache = imageLoader.diskCache
				val snapshot = diskCache?.openSnapshot(coverId)
				val imageBytes = snapshot.use {
					it?.data?.toFile()?.readBytes()
				}
				if (imageBytes != null) {
					return this.decodeBitmap(imageBytes)
				}
			} catch (e: Exception) {
				Logger.w("BitmapLoader", "could not read artwork data", e)
			}
		}

		return bitmapLoader.loadBitmap(uri)
	}
}
