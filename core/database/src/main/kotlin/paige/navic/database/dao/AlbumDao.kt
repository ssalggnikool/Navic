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
import kotlinx.coroutines.flow.Flow
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.relation.AlbumWithSongs
import paige.navic.util.Logger
import kotlin.time.Instant

@Dao
interface AlbumDao {
	@RawQuery
	fun getAlbumsByQuery(query: RoomRawQuery): PagingSource<Int, AlbumWithSongs>

	@RawQuery
	suspend fun getAlbumIdsByQuery(query: RoomRawQuery): List<String>

	@RawQuery(observedEntities = [AlbumEntity::class])
	fun getAlbumLetterCounts(query: RoomRawQuery): Flow<List<LetterCount>>

	@Transaction
	@Query("SELECT COUNT(albumId) FROM AlbumEntity")
	suspend fun getAlbumCount(): Int

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE albumId = :albumId LIMIT 1")
	suspend fun getAlbumById(albumId: String): AlbumWithSongs?

	@Query("SELECT * FROM AlbumEntity WHERE albumId = :albumId LIMIT 1")
	suspend fun getAlbumEntityById(albumId: String): AlbumEntity?

	@Query("SELECT * FROM AlbumEntity ORDER BY name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getAlbumsPage(limit: Int, offset: Int): List<AlbumEntity>

	@Query("SELECT * FROM AlbumEntity ORDER BY createdAt DESC, name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getNewestAlbumsPage(limit: Int, offset: Int): List<AlbumEntity>

	@Query("""
		SELECT * FROM AlbumEntity
		ORDER BY (
			(
				COALESCE(unicode(substr(albumId, 1, 1)), 0) * 31 +
				COALESCE(unicode(substr(albumId, 2, 1)), 0) * 131 +
				COALESCE(unicode(substr(albumId, 3, 1)), 0) * 137 +
				COALESCE(unicode(substr(albumId, 4, 1)), 0) * 149 +
				COALESCE(unicode(substr(albumId, 5, 1)), 0) * 151 +
				COALESCE(unicode(substr(albumId, 6, 1)), 0) * 157 +
				COALESCE(unicode(substr(albumId, 7, 1)), 0) * 163 +
				COALESCE(unicode(substr(albumId, 8, 1)), 0) * 167
			) * :seed
		) % 2147483647, albumId
		LIMIT :limit OFFSET :offset
	""")
	suspend fun getRandomAlbumsPage(seed: Int, limit: Int, offset: Int): List<AlbumEntity>

	@Query("SELECT * FROM AlbumEntity WHERE playCount != 0 ORDER BY playCount DESC, name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getFrequentAlbumsPage(limit: Int, offset: Int): List<AlbumEntity>

	@Query("SELECT * FROM AlbumEntity WHERE starredAt IS NOT NULL ORDER BY starredAt DESC, name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getStarredAlbumsPage(limit: Int, offset: Int): List<AlbumEntity>

	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId ORDER BY year DESC, name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getAlbumsByArtistId(artistId: String, limit: Int, offset: Int): List<AlbumEntity>

	@Query("SELECT EXISTS(SELECT 1 FROM AlbumEntity WHERE albumId = :albumId AND starredAt IS NOT NULL)")
	suspend fun isAlbumStarred(albumId: String): Boolean

	@Query("SELECT userRating FROM AlbumEntity WHERE albumId = :albumId")
	suspend fun getAlbumRating(albumId: String): Int?

	@Query("UPDATE AlbumEntity SET starredAt = :starredAt WHERE albumId = :albumId")
	suspend fun updateAlbumStarredAt(albumId: String, starredAt: Instant?)

	@Query("UPDATE AlbumEntity SET userRating = :userRating WHERE albumId = :albumId")
	suspend fun updateAlbumRating(albumId: String, userRating: Int?)

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId ORDER BY year DESC")
	fun getAlbumsByArtist(artistId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistName = :artistName ORDER BY year DESC")
	fun getAlbumsByArtistName(artistName: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND albumId != :albumId ORDER BY year DESC")
	fun getAlbumsByArtistExcluding(artistId: String, albumId: String): Flow<List<AlbumWithSongs>>

	@Query("SELECT * FROM AlbumEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun searchAlbumsPage(query: String, limit: Int, offset: Int): List<AlbumEntity>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	suspend fun searchAlbumsList(query: String): List<AlbumWithSongs>

	@Query("SELECT COUNT(*) FROM AlbumEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	suspend fun searchAlbumCount(query: String): Int

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbum(album: AlbumEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbums(albums: List<AlbumEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAlbumsIgnoringConflicts(albums: List<AlbumEntity>)

	@Query("DELETE FROM AlbumEntity WHERE albumId = :albumId")
	suspend fun deleteAlbum(albumId: String)

	@Query("DELETE FROM AlbumEntity")
	suspend fun clearAllAlbums()

	@Query("SELECT albumId FROM AlbumEntity")
	suspend fun getAllAlbumIds(): List<String>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE albumId IN (:ids)")
	suspend fun getAlbumsByIds(ids: List<String>): List<AlbumWithSongs>

	@Transaction
	suspend fun deleteObsoleteAlbums(remoteIds: Set<String>) {
		getAllAlbumIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("AlbumDao", "album $localId no longer exists remotely")
				deleteAlbum(localId)
			}
		}
	}
}
