/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord.uploader.catbox

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import paige.navic.discord.CoverArtImage
import paige.navic.discord.CoverArtUploader
import paige.navic.discord.appendImage
import paige.navic.discord.toPublicImageUrl

abstract class CatboxAbstractUploader(
	private val client: HttpClient
) : CoverArtUploader {
	abstract val endpoint: String
	abstract val time: String?

	override suspend fun upload(image: CoverArtImage): String {
		val response = client.submitFormWithBinaryData(
			url = endpoint,
			formData = formData {
				append("reqtype", "fileupload")
				time?.let { append("time", it) }
				appendImage("fileToUpload", image)
			}
		)

		if (!response.status.isSuccess()) {
			error("Catbox upload failed: ${response.status}")
		}

		return response.bodyAsText().toPublicImageUrl() ?: error("Catbox returned an invalid URL")
	}
}
