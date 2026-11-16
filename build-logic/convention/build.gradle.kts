/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

plugins {
	`kotlin-dsl`
}

group = "paige.navic.buildlogic"

kotlin {
	jvmToolchain(21)
}

dependencies {
	compileOnly(libs.plugins.android.application)
	compileOnly(libs.plugins.android.library)
	compileOnly(libs.plugins.compose.compiler)
}

tasks {
	validatePlugins {
		enableStricterValidation = true
		failOnWarning = true
	}
}

gradlePlugin {
	plugins {
		register("applicationCompose") {
			id = libs.plugins.navic.application.compose.get().pluginId
			implementationClass = "ApplicationComposeConventionPlugin"
		}
		register("androidApplication") {
			id = libs.plugins.navic.application.asProvider().get().pluginId
			implementationClass = "ApplicationConventionPlugin"
		}

		register("composeLibrary") {
			id = libs.plugins.navic.library.compose.get().pluginId
			implementationClass = "ComposeLibraryConventionPlugin"
		}
		register("androidLibrary") {
			id = libs.plugins.navic.library.asProvider().get().pluginId
			implementationClass = "LibraryConventionPlugin"
		}

		register("feature") {
			id = libs.plugins.navic.feature.get().pluginId
			implementationClass = "FeatureConventionPlugin"
		}
	}
}

fun DependencyHandler.compileOnly(dependency: Provider<PluginDependency>) {
	compileOnly(dependency.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
