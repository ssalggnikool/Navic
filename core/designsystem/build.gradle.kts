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
	namespace = "paige.navic.core.designsystem"
}

dependencies {
	implementation(project(":core:preferences"))
	implementation(project(":core:common"))

	implementation(libs.compose.runtime)
	implementation(libs.compose.foundation)
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.koin.core)
	implementation(libs.koin.android)
	implementation(libs.koin.compose)
	implementation(libs.materialKolor)
}
