/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord.uploader

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import paige.navic.discord.CoverArtImage
import paige.navic.discord.CoverArtUploader
import paige.navic.discord.appendImage
import paige.navic.discord.toPublicImageUrl

class FreeImageHostUploader(
	private val client: HttpClient
) : CoverArtUploader {
	private companion object {
		const val API_KEY = "6d207e02198a847aa98d0a2a901485a5"
	}

	override suspend fun upload(image: CoverArtImage): String {
		val response = client.submitFormWithBinaryData(
			url = "https://freeimage.host/api/1/upload",
			formData = formData {
				append("key", API_KEY)
				append("action", "upload")
				append("format", "json")
				appendImage("source", image)
			}
		)

		if (!response.status.isSuccess()) {
			error("FreeImageHost upload failed: ${response.status}")
		}

		val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
		return json["image"]
			?.jsonObject
			?.get("url")
			?.jsonPrimitive
			?.contentOrNull
			?.toPublicImageUrl()
			?: error("FreeImageHost returned an invalid URL")
	}
}
