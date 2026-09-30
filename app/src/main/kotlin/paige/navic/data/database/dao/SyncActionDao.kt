/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entity.SyncActionEntity

@Dao
interface SyncActionDao {
	@Insert
	suspend fun enqueue(action: SyncActionEntity)

	@Transaction
	@Query("SELECT * FROM SyncActionEntity ORDER BY id ASC")
	suspend fun getPendingActions(): List<SyncActionEntity>

	@Query("DELETE FROM SyncActionEntity WHERE id = :id")
	suspend fun removeAction(id: Int)

	@Query("DELETE FROM SyncActionEntity")
	suspend fun clearAllActions()
}
