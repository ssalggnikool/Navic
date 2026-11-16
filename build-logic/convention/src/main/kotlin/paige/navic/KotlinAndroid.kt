/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import paige.navic.ext.androidMinSdk
import paige.navic.ext.androidSdk
import paige.navic.ext.libs

fun Project.configureKotlinAndroid(
	commonExtension: CommonExtension,
	explicitApi: Boolean = false
) {
	commonExtension.apply {
		val (major, minor) = libs.androidSdk
		compileSdk = major
		compileSdkMinor = minor

		defaultConfig.apply {
			minSdk = libs.androidMinSdk
			// ?????? why does this not work
			//targetSdk = libs.androidTargetSdk
		}

		compileOptions.apply {
			isCoreLibraryDesugaringEnabled = true
			sourceCompatibility = JavaVersion.VERSION_21
			targetCompatibility = JavaVersion.VERSION_21
		}
	}

	configureKotlin<KotlinAndroidProjectExtension>(explicitApi)

	dependencies {
		"coreLibraryDesugaring"(libs.findLibrary("desugar_jdk_libs").get())
	}
}

private inline fun <reified T: KotlinBaseExtension> Project.configureKotlin(
	explicitApi: Boolean
) = configure<T> {
	when (this) {
		is KotlinAndroidProjectExtension -> compilerOptions
		else -> TODO("Unsupported Kotlin project extension: $this (${this::class})")
	}.apply {
		jvmToolchain(21)
		compilerOptions {
			// sheet motion specs and expand progress have no public Material3 API yet
			freeCompilerArgs.add("-Xwarning-level=ERROR_SUPPRESSION:disabled")
		}
	}
}
