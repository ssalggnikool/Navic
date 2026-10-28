/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord.uploader

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.parameter
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

class ImgBbUploader(
	private val client: HttpClient,
	private val apiKey: String
) : CoverArtUploader {
	override suspend fun upload(image: CoverArtImage): String {
		val response = client.submitFormWithBinaryData(
			url = "https://api.imgbb.com/1/upload",
			formData = formData {
				appendImage("image", image)
			}
		) {
			parameter("key", apiKey)
		}

		if (!response.status.isSuccess()) {
			error("ImgBB upload failed: ${response.status}")
		}

		val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
		return json["data"]
			?.jsonObject
			?.get("url")
			?.jsonPrimitive
			?.contentOrNull
			?.toPublicImageUrl()
			?: error("ImgBB returned an invalid URL")
	}
}
