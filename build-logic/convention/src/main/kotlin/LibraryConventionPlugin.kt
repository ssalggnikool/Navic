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

class LibraryConventionPlugin: Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.android.library")

			extensions.configure<LibraryExtension> {
				configureKotlinAndroid(this, explicitApi = true)

				// Converts a module's path (i.e. :core:ui)
				// to a valid resource prefix (i.e. core_ui_)
				resourcePrefix = path
					.split("""\W""".toRegex())
					.drop(1)
					.distinct()
					.joinToString(separator = "_")
					.lowercase() + "_"
			}
		}
	}
}
