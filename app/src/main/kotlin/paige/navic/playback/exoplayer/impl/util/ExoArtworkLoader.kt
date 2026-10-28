/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.impl.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceBitmapLoader
import coil3.ImageLoader
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import paige.navic.domain.manager.runtime.StorageManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.util.Logger

/**
 * Small util for attempting to load cached artworks from Coil first,
 * before resorting to doing the normal behavior of ExoPlayer (which just fetches the provided image url)
 */
@OptIn(UnstableApi::class)
class ExoArtworkLoader(
	context: Context,
	dataSourceFactory: DataSource.Factory,
	private val imageLoader: ImageLoader,
	private val sessionManager: SessionManager,
	private val storageManager: StorageManager,
): BitmapLoader {
	private val bitmapLoader = DataSourceBitmapLoader.Builder(context)
		.setDataSourceFactory(dataSourceFactory)
		.build()

	override fun supportsMimeType(mimeType: String): Boolean {
		return bitmapLoader.supportsMimeType(mimeType)
	}

	override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
		return bitmapLoader.decodeBitmap(data)
	}

	override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
		val coverId = uri.lastPathSegment

		if (coverId != null) {
			try {
				val localFile = storageManager.getCoverArtFile(coverId)
				if (localFile != null) {
					return this.decodeBitmap(localFile.readBytes())
				}

				val diskCache = imageLoader.diskCache
				val snapshot = diskCache?.openSnapshot(coverId)
					?: listOf("1024", "4096").firstNotNullOfOrNull { size ->
						diskCache?.openSnapshot("$coverId#size=$size")
					}
				val imageBytes = snapshot.use {
					it?.data?.toFile()?.readBytes()
				}
				if (imageBytes != null) {
					return this.decodeBitmap(imageBytes)
				}
			} catch (e: Exception) {
				Logger.w("BitmapLoader", "could not read artwork data", e)
			}

			val actualUrl = sessionManager.getCoverArtUrl(coverId)
			return bitmapLoader.loadBitmap(actualUrl.toUri())
		}

		return Futures.immediateFuture(null)
	}
}
