/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import paige.navic.database.CacheDatabase

val databaseModule = module {
	single<CacheDatabase> {
		val context = androidApplication()
		context.deleteDatabase("downloads.db")
		val dbPath = context
			.getDatabasePath("cache.db")
			.absolutePath
		Room
			.databaseBuilder<CacheDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single { get<CacheDatabase>().albumDao() }
	single { get<CacheDatabase>().artistDao() }
	single { get<CacheDatabase>().genreDao() }
	single { get<CacheDatabase>().lyricDao() }
	single { get<CacheDatabase>().playlistDao() }
	single { get<CacheDatabase>().radioDao() }
	single { get<CacheDatabase>().songDao() }
	single { get<CacheDatabase>().syncActionDao() }
	single { get<CacheDatabase>().downloadDao() }
}
