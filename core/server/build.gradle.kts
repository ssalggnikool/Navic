/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	alias(libs.plugins.navic.library)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "paige.navic.core.server"
}

dependencies {
	api(project(":core:model"))
	implementation(project(":core:common"))
	implementation(project(":core:database"))

	implementation(libs.subsonicKotlin)
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.contentNegotiation)
	implementation(libs.ktor.serialization.json)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.androidx.media3.common)
	implementation(libs.androidx.media3.decoder.ffmpeg)
	implementation(libs.androidx.annotation)
	implementation(libs.androidx.annotation.experimental)
}
