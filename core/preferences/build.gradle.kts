/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	alias(libs.plugins.navic.library)
	alias(libs.plugins.navic.library.compose)
}

android {
	namespace = "paige.navic.core.preferences"
}

dependencies {
	api(project(":core:model"))
	implementation(project(":core:common"))

	implementation(libs.compose.runtime)
	implementation(libs.compose.material3)
	implementation(libs.multiplatformSettings)
	implementation(libs.materialKolor)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.koin.core)
	implementation(libs.koin.compose)
	implementation(libs.koin.android)
}
