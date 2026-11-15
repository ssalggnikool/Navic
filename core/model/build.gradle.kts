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
	namespace = "paige.navic.core.model"
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
	implementation(libs.compose.runtime)
	implementation(libs.androidx.annotation)
	implementation(libs.kotlinx.serialization.json)
}
