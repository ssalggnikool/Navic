/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	alias(libs.plugins.navic.library)
	alias(libs.plugins.navic.library.compose)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "paige.navic.core.model"
}

dependencies {
	implementation(project(":core:common"))

	implementation(libs.compose.ui)
	implementation(libs.compose.runtime)
	implementation(libs.compose.foundation)
	implementation(libs.compose.material3)
	implementation(libs.androidx.annotation)
	implementation(libs.kotlinx.serialization.json)
}
