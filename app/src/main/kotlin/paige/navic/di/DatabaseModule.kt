package paige.navic.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.data.database.DownloadDatabase

val databaseModule = module {
	single<CacheDatabase> {
		val dbPath = androidApplication()
			.getDatabasePath("cache.db")
			.absolutePath
		Room
			.databaseBuilder<CacheDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single<DownloadDatabase> {
		val dbPath = androidApplication()
			.getDatabasePath("downloads.db")
			.absolutePath
		Room
			.databaseBuilder<DownloadDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single { get<CacheDatabase>().albumDao() }
	single { get<CacheDatabase>().genreDao() }
	single { get<CacheDatabase>().playlistDao() }
	single { get<CacheDatabase>().songDao() }
	single { get<CacheDatabase>().artistDao() }
	single { get<CacheDatabase>().radioDao() }
	single { get<CacheDatabase>().lyricDao() }
	single { get<CacheDatabase>().syncActionDao() }
	single { get<DownloadDatabase>().downloadDao() }
}
