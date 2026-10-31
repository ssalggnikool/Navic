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
import paige.navic.database.entity.PlayableSongRow
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.entity.PlaylistSongCrossRef
import paige.navic.database.relation.PlaylistWithSongs
import paige.navic.util.Logger

@Dao
interface PlaylistDao {
	@Transaction
	@RawQuery
	fun getPlaylistsByQuery(query: RoomRawQuery): PagingSource<Int, PlaylistWithSongs>

	@RawQuery
	suspend fun getPlaylistIdsByQuery(query: RoomRawQuery): List<String>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE playlistId IN (:ids)")
	suspend fun getPlaylistsByIds(ids: List<String>): List<PlaylistWithSongs>

	@Query("SELECT * FROM PlaylistEntity ORDER BY name COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun getPlaylistsPage(limit: Int, offset: Int): List<PlaylistEntity>

	@Query("SELECT COUNT(*) FROM PlaylistEntity")
	suspend fun getPlaylistCount(): Int

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylist(playlist: PlaylistEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE playlistId = :playlistId LIMIT 1")
	suspend fun getPlaylistById(playlistId: String): PlaylistWithSongs?

	@Query("SELECT * FROM PlaylistEntity WHERE playlistId = :playlistId LIMIT 1")
	suspend fun getPlaylistEntityById(playlistId: String): PlaylistEntity?

	@Query("DELETE FROM PlaylistEntity WHERE playlistId = :playlistId")
	suspend fun deletePlaylist(playlistId: String)

	@Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId")
	suspend fun deletePlaylistSongCrossRefs(playlistId: String)

	@Transaction
	suspend fun replacePlaylistSongs(playlistId: String, crossRefs: List<PlaylistSongCrossRef>) {
		deletePlaylistSongCrossRefs(playlistId)
		insertPlaylistSongCrossRefs(crossRefs)
	}

	@Query("DELETE FROM PlaylistEntity")
	suspend fun clearAllPlaylists()

	@Query("SELECT playlistId FROM PlaylistEntity")
	suspend fun getAllPlaylistIds(): List<String>

	@Query("SELECT * FROM PlaylistEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE LIMIT :limit OFFSET :offset")
	suspend fun searchPlaylistsPage(query: String, limit: Int, offset: Int): List<PlaylistEntity>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	suspend fun searchPlaylistsList(query: String): List<PlaylistWithSongs>

	@Query("SELECT COUNT(*) FROM PlaylistEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	suspend fun searchPlaylistCount(query: String): Int

	@Query("""
		SELECT s.* FROM SongEntity s
		INNER JOIN PlaylistSongCrossRef ps ON s.songId = ps.songId
		WHERE ps.playlistId = :playlistId
		ORDER BY ps.position ASC
		LIMIT :limit OFFSET :offset
	""")
	suspend fun getPlaylistSongs(playlistId: String, limit: Int, offset: Int): List<paige.navic.database.entity.SongEntity>

	@Query("""
		SELECT s.songId, s.title, s.artistName, s.albumTitle, s.coverArtId, s.duration, s.filePath, s.artists, s.albumArtists
		FROM SongEntity s
		INNER JOIN PlaylistSongCrossRef ps ON s.songId = ps.songId
		WHERE ps.playlistId = :playlistId
		ORDER BY ps.position ASC
		LIMIT :limit OFFSET :offset
	""")
	suspend fun getPlaylistPlayableSongs(
		playlistId: String,
		limit: Int,
		offset: Int
	): List<PlayableSongRow>

	@Transaction
	suspend fun deleteObsoletePlaylists(remoteIds: Set<String>) {
		getAllPlaylistIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("PlaylistDao", "playlist $localId no longer exists remotely")
				deletePlaylist(localId)
			}
		}
	}
}
