/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import paige.navic.configureCompose

class ApplicationComposeConventionPlugin: Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.android.application")
			apply(plugin = "org.jetbrains.kotlin.plugin.compose")

			val extension = extensions.getByType<ApplicationExtension>()
			configureCompose(extension)
		}
	}
}
