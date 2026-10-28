/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord

import io.ktor.client.request.forms.FormBuilder
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.overviewproject.mime_types.MimeTypeDetector
import java.io.ByteArrayInputStream

data class CoverArtImage(
	val bytes: ByteArray,
	val contentType: ContentType,
	val fileName: String
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as CoverArtImage

		if (!bytes.contentEquals(other.bytes)) return false
		if (contentType != other.contentType) return false
		if (fileName != other.fileName) return false

		return true
	}

	override fun hashCode(): Int {
		var result = bytes.contentHashCode()
		result = 31 * result + contentType.hashCode()
		result = 31 * result + fileName.hashCode()
		return result
	}
}

interface CoverArtUploader {
	suspend fun upload(image: CoverArtImage): String
}

object MimeTypeUtils {
	private val detector = MimeTypeDetector()

	fun detectFromMagic(byteArray: ByteArray): String? {
		return detector.detectMimeType("filename", ByteArrayInputStream(byteArray))
	}
}

fun ByteArray.toCoverArtImage(): CoverArtImage? {
	val mimetype = MimeTypeUtils.detectFromMagic(
		this.slice(0..2048).toByteArray()
	)?.split("/")?.takeIf { it.size > 1 }

	val contentType = mimetype?.let {
		ContentType(mimetype[0], mimetype[1])
	} ?: return null

	return CoverArtImage(
		bytes = this,
		contentType = contentType,
		fileName = "cover.${contentType.contentSubtype}"
	)
}

fun String.toPublicImageUrl(): String? =
	trim().takeIf { it.startsWith("https://") && length < 128 }

fun FormBuilder.appendImage(field: String, image: CoverArtImage) {
	append(
		key = field,
		value = image.bytes,
		headers = Headers.build {
			append(HttpHeaders.ContentType, image.contentType.toString())
			append(HttpHeaders.ContentDisposition, "filename=\"${image.fileName}\"")
		}
	)
}
