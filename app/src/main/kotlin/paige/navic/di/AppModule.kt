/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import paige.navic.ui.navigation.PersistentViewModelStoreOwner
import paige.navic.util.preferencesFile

val appModule = module {
	single<Settings> {
		val context = androidApplication()
		SharedPreferencesSettings(
			context.getSharedPreferences(
				preferencesFile(context),
				Context.MODE_PRIVATE
			)
		)
	}
	singleOf(::CoilSingleton)
	single { get<CoilSingleton>().coilImageLoader }
	single(named("static")) { get<CoilSingleton>().staticCoilImageLoader }
	singleOf(::PersistentViewModelStoreOwner)
}
