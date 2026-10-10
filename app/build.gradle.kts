/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.android.build.api.variant.impl.VariantOutputImpl
import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.androidx.room3)
}

configurations.all {
	// remove material 2
	exclude(group = "org.jetbrains.compose.material", module = "material")
	exclude(group = "androidx.compose.material", module = "material")
	// cache SNAPSHOT dependencies for less time, default 24h
	resolutionStrategy.cacheChangingModulesFor(1, "hours")
}

val generateBuildInfo = tasks.register("generateBuildInfo", Sync::class) {
	description = "generate BuildInfo.kt"

	val fdroid = System.getenv("FDROID") == "true" || providers.gradleProperty("fdroid")
		.map { it.toBoolean() }
		.getOrElse(false)

	from(
		resources.text.fromString(
			"""
			|package paige.navic.generated
			|
			|object BuildInfo {
			|	const val FDROID = $fdroid
			|}
			|
			""".trimMargin()
		)
	) {
		rename { "BuildInfo.kt" }
		into("paige/navic/generated")
	}

	into(layout.buildDirectory.dir("generated/buildInfo/commonMain/kotlin"))
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
	dependsOn(generateBuildInfo)
}

// no idea why ksp tasks depend on this
tasks.withType<KspAATask>().configureEach {
	dependsOn(generateBuildInfo)
}

android.sourceSets.named("main") {
	kotlin.directories.add(generateBuildInfo.map { it.destinationDir }.get().absolutePath)
}

kotlin {
	jvmToolchain(21)
}

val isTaskRelease = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

val fdroid = System.getenv("FDROID") == "true" || providers.gradleProperty("fdroid")
	.map { it.toBoolean() }
	.getOrElse(false)

android {
	namespace = "paige.navic"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	compileSdkMinor = libs.versions.android.compileSdkMinor.get().toInt()

	buildFeatures {
		resValues = true
	}

	defaultConfig {
		applicationId = "paige.navic"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 59
		versionName = "v1.0.0-alpha59"
		multiDexEnabled = true

		ndk {
			abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
			if (!isTaskRelease) {
				abiFilters.addAll(listOf("x86_64", "x86"))
			}
		}
	}

	signingConfigs {
		create("release") {
			keyAlias = System.getenv("SIGNING_KEY_ALIAS")
			keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
			storeFile = System.getenv("SIGNING_STORE_FILE")?.let(::File)
			storePassword = System.getenv("SIGNING_STORE_PASSWORD")
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isProfileable = false
			isJniDebuggable = false
			isShrinkResources = true
			signingConfig = signingConfigs.findByName("release")?.takeIf { it.storeFile != null }
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}

		debug {
			applicationIdSuffix = ".debug"
			resValue("string", "app_name", "Navic (Dev)")
		}
	}

	packaging {
		resources {
			excludes += "/okhttp3/**"
			excludes += "/*.properties"
			excludes += "/org/antlr/**"
			excludes += "/com/android/tools/smali/**"
			excludes += "/org/eclipse/jgit/**"
			excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
			excludes += "/org/bouncycastle/**"
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}

		jniLibs {
			keepDebugSymbols.add("**/*.so")
		}
	}

	compileOptions {
		isCoreLibraryDesugaringEnabled = true

		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}
}

androidComponents {
	onVariants { variant ->
		variant.outputs.forEach { output ->
			if (output is VariantOutputImpl) {
				output.outputFileName = if (fdroid) {
					"Navic.fdroid.apk"
				} else {
					"Navic.apk"
				}
			}
		}
	}
	onVariants(selector().withBuildType("release")) {
		it.packaging.resources.excludes.apply {
			add("/**/*.version")
			add("/kotlin-tooling-metadata.json")
			add("/DebugProbesKt.bin")
			add("/**/*.kotlin_builtins")
		}
	}
}

room3 {
	schemaDirectory("$projectDir/schemas")
}

dependencies {
	// Compose
	implementation(libs.compose.runtime)
	implementation(libs.compose.foundation)
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.compose.material3.adaptive.nav)
	implementation(libs.compose.material3.windowSize)

	// Androidx
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.lifecycle.viewmodel)
	implementation(libs.androidx.lifecycle.runtime)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.animation.graphics)
	implementation(libs.androidx.sqlite.bundled)
	implementation(libs.androidx.room3.runtime)
	implementation(libs.androidx.navigation3.ui)
	implementation(libs.androidx.media3.exoplayer)
	implementation(libs.androidx.media3.session)
	implementation(libs.androidx.media3.ktor)
	implementation(libs.androidx.media3.decoder.ffmpeg)
	implementation(libs.androidx.annotation)
	implementation(libs.androidx.glance.appwidget)
	implementation(libs.androidx.glance.material3)
	implementation(libs.androidx.browser)

	// Kotlinx
	implementation(libs.kotlinx.datetime)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.collections.immutable)

	// Networking
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.okhttp)
	implementation(libs.ktor.client.contentNegotiation)
	implementation(libs.ktor.serialization.json)
	implementation(libs.coil.compose)
	implementation(libs.coil.network.ktor3)
	implementation(libs.coil.gif)

	// Compose 3rd party
	implementation(libs.capsule)
	implementation(libs.kmpalette.core)
	implementation(libs.kmpalette.network)
	implementation(libs.materialKolor)
	implementation(libs.composePipette)
	implementation(libs.multiplatformSettings)

	// Misc
	implementation(libs.koin.core)
	implementation(libs.koin.android)
	implementation(libs.koin.compose)
	implementation(libs.koin.compose.viewmodel)
	implementation(libs.subsonicKotlin)
	ksp(libs.androidx.room3.compiler)
	coreLibraryDesugaring(libs.desugar.jdk.libs)
}
