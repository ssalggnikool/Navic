/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import paige.navic.database.dao.DownloadDao
import paige.navic.database.entity.DownloadEntity

@Database(
	version = 3,
	entities = [DownloadEntity::class]
)
abstract class DownloadDatabase : RoomDatabase() {
	abstract fun downloadDao(): DownloadDao
}
