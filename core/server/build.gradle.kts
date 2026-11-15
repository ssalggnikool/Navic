/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	id("com.android.library")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "paige.navic.core.server"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	compileSdkMinor = libs.versions.android.compileSdkMinor.get().toInt()

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

kotlin {
	jvmToolchain(21)
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
