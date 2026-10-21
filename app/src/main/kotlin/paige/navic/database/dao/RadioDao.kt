/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.database.entity.RadioEntity
import paige.navic.util.Logger

@Dao
interface RadioDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRadios(radios: List<RadioEntity>)

	@Query("SELECT * FROM RadioEntity ORDER BY name ASC")
	fun getRadios(): PagingSource<Int, RadioEntity>

	@Query("DELETE FROM RadioEntity WHERE radioId = :radioId")
	suspend fun deleteRadio(radioId: String)

	@Query("DELETE FROM RadioEntity")
	suspend fun clearAllRadios()

	@Query("SELECT radioId FROM RadioEntity")
	suspend fun getAllRadioIds(): List<String>

	@Transaction
	suspend fun deleteObsoleteRadios(remoteIds: Set<String>) {
		getAllRadioIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("RadioDao", "Radio $localId no longer exists remotely")
				deleteRadio(localId)
			}
		}
	}
}
