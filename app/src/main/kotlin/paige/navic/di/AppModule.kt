/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import paige.navic.ui.navigation.PersistentViewModelStoreOwner

val appModule = module {
	single { Settings() }
	singleOf(::CoilSingleton)
	single { get<CoilSingleton>().coilImageLoader }
	single(named("static")) { get<CoilSingleton>().staticCoilImageLoader }
	singleOf(::PersistentViewModelStoreOwner)
}
