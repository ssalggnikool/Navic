/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import paige.navic.configureKotlinAndroid

class FeatureConventionPlugin: Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "navic.library")
			apply(plugin = "navic.library.compose")

			extensions.configure<LibraryExtension> {
				configureKotlinAndroid(this)

				// Converts a feature module's path (i.e. :feature:settings)
				// to a valid resource prefix (i.e. settings_)
				resourcePrefix = path
					.split("""\W""".toRegex())
					.drop(2)
					.distinct()
					.joinToString(separator = "_")
					.lowercase() + "_"
			}
		}
	}
}
