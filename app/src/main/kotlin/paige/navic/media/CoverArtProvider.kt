/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.media

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import paige.navic.domain.manager.session.SessionManager
import java.io.File

class CoverArtProvider : ContentProvider() {
	private val imageLoader: ImageLoader by inject()
	private val sessionManager: SessionManager by inject()

	override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
		require(mode == "r") { "Cover art is read-only" }
		val coverId = uri.pathSegments.singleOrNull()
			?: error("Invalid cover art URI")
		val file = renderCover(coverId, uri.artworkSize())
		return try {
			ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
		} finally {
			file.delete()
		}
	}

	private fun Uri.artworkSize(): Int = getQueryParameter("size")
		?.toIntOrNull()
		?.coerceIn(MIN_ARTWORK_SIZE, MAX_ARTWORK_SIZE)
		?: DEFAULT_ARTWORK_SIZE

	private fun renderCover(coverId: String, size: Int): File = runBlocking(Dispatchers.IO) {
		val context = requireNotNull(context)
		val request = ImageRequest.Builder(context)
			.data(sessionManager.getCoverArtUrl(coverId))
			.diskCacheKey(coverId)
			.size(size)
			.allowHardware(false)
			.build()

		val bitmap = checkNotNull(imageLoader.execute(request).image?.toBitmap()) {
			"Could not load cover art $coverId"
		}

		val quality =
			if (size > DEFAULT_ARTWORK_SIZE) HIGH_ARTWORK_QUALITY else DEFAULT_ARTWORK_QUALITY

		val file = File.createTempFile("cover-", ".jpg", context.cacheDir)
		try {
			file.outputStream().use { output ->
				check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
					"Could not encode cover art $coverId"
				}
			}
			file
		} catch (e: Exception) {
			file.delete()
			throw e
		}
	}

	override fun onCreate() = true
	override fun getType(uri: Uri) = "image/*"
	override fun query(
		uri: Uri,
		projection: Array<out String>?,
		selection: String?,
		selectionArgs: Array<out String>?,
		sortOrder: String?
	): Cursor? = null
	override fun insert(uri: Uri, values: ContentValues?) = null
	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
	override fun update(
		uri: Uri,
		values: ContentValues?,
		selection: String?,
		selectionArgs: Array<out String>?
	) = 0

	private companion object {
		const val DEFAULT_ARTWORK_SIZE = 512
		const val MIN_ARTWORK_SIZE = 128
		const val MAX_ARTWORK_SIZE = 1024
		const val DEFAULT_ARTWORK_QUALITY = 80
		const val HIGH_ARTWORK_QUALITY = 90
	}
}
