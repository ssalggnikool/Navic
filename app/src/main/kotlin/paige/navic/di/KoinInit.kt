/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
	return startKoin {
		config?.invoke(this)
		printLogger()
		modules(
			appModule,
			databaseModule,
			dataStoreModule,
			managerModule,
			repositoryModule,
			viewModelModule
		)
		koin.createEagerInstances()
	}
}
