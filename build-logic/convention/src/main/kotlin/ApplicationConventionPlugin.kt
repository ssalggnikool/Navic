/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import paige.navic.configureKotlinAndroid
import paige.navic.ext.androidSdk
import paige.navic.ext.libs

class ApplicationConventionPlugin: Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.android.application")

			extensions.configure<ApplicationExtension> {
				configureKotlinAndroid(this)
				defaultConfig.targetSdk = libs.androidSdk.first()
			}
		}
	}
}
