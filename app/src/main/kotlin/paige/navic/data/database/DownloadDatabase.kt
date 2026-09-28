package paige.navic.data.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.entities.DownloadEntity

@Database(
	version = 3,
	entities = [DownloadEntity::class]
)
abstract class DownloadDatabase : RoomDatabase() {
	abstract fun downloadDao(): DownloadDao
}
