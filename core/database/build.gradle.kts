/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	id("com.android.library")
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.androidx.room3)
}

android {
	namespace = "paige.navic.core.database"
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

room3 {
	schemaDirectory("$projectDir/schemas")
}

dependencies {
	api(project(":core:model"))
	implementation(project(":core:common"))

	api(libs.androidx.room3.runtime)
	api(libs.androidx.paging.common)
	implementation(libs.androidx.room3.paging)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.serialization.json)

	ksp(libs.androidx.room3.compiler)
}
