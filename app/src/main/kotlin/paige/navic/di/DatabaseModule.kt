/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import paige.navic.database.CacheDatabase
import paige.navic.domain.manager.session.AccountStore
import paige.navic.util.Logger
import java.io.File

private const val ANONYMOUS_ACCOUNT = "anonymous"

private fun databaseName(accountId: String) = "cache_$accountId.db"

private fun migrateLegacyDatabase(context: Context, legacyAccountId: String) {
	val legacy = context.getDatabasePath("cache.db")
	if (!legacy.exists()) return

	val target = context.getDatabasePath(databaseName(legacyAccountId))
	if (target.exists()) return

	listOf("", "-wal", "-shm", "-journal").forEach { suffix ->
		val source = File(legacy.path + suffix)
		if (source.exists() && !source.renameTo(File(target.path + suffix))) {
			Logger.e("DatabaseModule", "failed to migrate ${source.name}")
		}
	}
}

val databaseModule = module {
	single<CacheDatabase> {
		val context = androidApplication()
		context.deleteDatabase("downloads.db")

		val accountStore: AccountStore = get()
		accountStore.claimDatabaseDeletions().forEach { id ->
			context.deleteDatabase(databaseName(id))
		}
		accountStore.legacyAccountId?.let { legacyAccountId ->
			migrateLegacyDatabase(context, legacyAccountId)
		}

		val accountId = accountStore.activeAccountId ?: ANONYMOUS_ACCOUNT
		val dbPath = context
			.getDatabasePath(databaseName(accountId))
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
