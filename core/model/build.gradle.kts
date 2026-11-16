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
	implementation(libs.compose.runtime)
	implementation(libs.androidx.annotation)
	implementation(libs.kotlinx.serialization.json)
}
