/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("UnstableApiUsage")

import androidx.media3.buildlogic.includeMedia3

rootProject.name = "Navic"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	includeBuild("androidx-media/build-logic-settings")
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("gradlebuild.media3-settings-logic")
}

dependencyResolutionManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		maven {
			name = "Central Portal Snapshots"
			url = uri("https://central.sonatype.com/repository/maven-snapshots/")
			content {
				includeGroupAndSubgroups("dev.zt64.subsonic")
			}
		}
		maven {
			name = "Jitpack"
			url = uri("https://jitpack.io")
			content {
				includeGroupAndSubgroups("com.github.Nightdavisao")
			}
		}
		mavenCentral()
	}
}

include(":app")
includeMedia3(file("androidx-media"))
