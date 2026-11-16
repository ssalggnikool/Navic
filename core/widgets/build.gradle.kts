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
	namespace = "paige.navic.core.widgets"
}

dependencies {
	implementation(libs.androidx.glance.appwidget)
	implementation(libs.androidx.glance.material3)
	implementation(libs.coil.compose)
}
