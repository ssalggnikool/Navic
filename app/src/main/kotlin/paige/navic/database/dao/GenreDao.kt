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
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import paige.navic.database.entity.GenreEntity
import paige.navic.util.Logger

@Dao
interface GenreDao {
	@RawQuery
	fun getGenresByQuery(query: RoomRawQuery): PagingSource<Int, GenreEntity>

	@Query(
		"""
		SELECT coverArtId FROM AlbumEntity
		WHERE (genre = :genreName OR genres LIKE '%' || :genreName || '%')
		AND coverArtId IS NOT NULL
		LIMIT 2
		"""
	)
	suspend fun getGenreCoverArtIds(genreName: String): List<String>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertGenres(genres: List<GenreEntity>)

	@Query("DELETE FROM GenreEntity WHERE genreName = :genreName")
	suspend fun deleteGenre(genreName: String)

	@Query("DELETE FROM GenreEntity")
	suspend fun clearAllGenres()

	@Query("SELECT genreName FROM GenreEntity")
	suspend fun getAllGenreNames(): List<String>

	@Transaction
	suspend fun deleteObsoleteGenres(remoteNames: Set<String>) {
		getAllGenreNames().forEach { localName ->
			if (localName !in remoteNames) {
				Logger.w("GenreDao", "genre $localName no longer exists remotely")
				deleteGenre(localName)
			}
		}
	}
}
