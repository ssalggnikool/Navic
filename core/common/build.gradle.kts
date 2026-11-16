/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	alias(libs.plugins.navic.library)
}

android {
	namespace = "paige.navic.core.common"
}

dependencies {
	implementation(libs.androidx.annotation)
}
