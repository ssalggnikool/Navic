/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entity.LyricEntity

@Dao
interface LyricDao {
	@Transaction
	@Query("SELECT * FROM LyricEntity WHERE songId = :songId")
	suspend fun getLyrics(songId: String): LyricEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLyrics(lyrics: LyricEntity)

	@Query("DELETE FROM LyricEntity")
	suspend fun clearAllLyrics()
}
