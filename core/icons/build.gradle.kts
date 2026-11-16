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
	namespace = "paige.navic.core.icons"
}

dependencies {
	implementation(libs.compose.ui)
	implementation(libs.compose.runtime)
	implementation(libs.compose.foundation)
}
