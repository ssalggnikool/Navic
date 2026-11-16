/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureCompose(
	commonExtension: CommonExtension
) {
	commonExtension.apply {
		buildFeatures.apply {
			compose = true
		}
	}

	@Suppress("UnstableApiUsage")
	extensions.configure<ComposeCompilerGradlePluginExtension> {
		fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }
		fun Provider<*>.relativeToRootProject(dir: String) = map {
			isolated.rootProject.projectDirectory
				.dir("build")
				.dir(projectDir.toRelativeString(rootDir))
		}.map { it.dir(dir) }

		project.providers.gradleProperty("enableComposeCompilerMetrics").onlyIfTrue()
			.relativeToRootProject("compose-metrics")
			.let(metricsDestination::set)

		project.providers.gradleProperty("enableComposeCompilerReports").onlyIfTrue()
			.relativeToRootProject("compose-reports")
			.let(reportsDestination::set)
	}
}
