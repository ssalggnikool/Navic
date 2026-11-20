/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ext

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
	get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val VersionCatalog.androidSdk
	get() = findVersion("android-sdk")
		.get().displayName
		.split(".")
		.map { it.toInt() }

val VersionCatalog.androidMinSdk
	get() = findVersion("android-minSdk")
		.get().displayName
		.toInt()

val VersionCatalog.androidTargetSdk
		get() = findVersion("android-targetSdk")
		.get().displayName
		.toInt()
