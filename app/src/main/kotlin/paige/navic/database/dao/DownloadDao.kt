/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus

@Dao
interface DownloadDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertDownload(download: DownloadEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertDownloads(downloads: List<DownloadEntity>)

	@Query("SELECT * FROM DownloadEntity WHERE songId = :songId")
	suspend fun getDownloadById(songId: String): DownloadEntity?

	@Query("SELECT * FROM DownloadEntity")
	fun getAllDownloads(): Flow<List<DownloadEntity>>

	@Query("SELECT * FROM DownloadEntity WHERE status = :status")
	suspend fun getDownloadsByStatus(status: DownloadStatus): List<DownloadEntity>

	@Query("SELECT COUNT(*) FROM DownloadEntity WHERE status = :status")
	suspend fun countDownloadsByStatus(status: DownloadStatus): Int

	@Query("SELECT COUNT(*) FROM DownloadEntity WHERE status = :status")
	fun getDownloadsCount(status: DownloadStatus = DownloadStatus.DOWNLOADED): Flow<Int>

	@Query("DELETE FROM DownloadEntity WHERE songId = :songId")
	suspend fun deleteDownload(songId: String)

	@Query("DELETE FROM DownloadEntity WHERE status = :status")
	suspend fun deleteDownloadsByStatus(status: DownloadStatus)

	@Query("UPDATE DownloadEntity SET status = :status, progress = :progress WHERE songId = :songId")
	suspend fun updateProgress(songId: String, status: DownloadStatus, progress: Float)

	@Query("DELETE FROM DownloadEntity")
	suspend fun clearAllDownloads()

	@Query("SELECT * FROM DownloadEntity WHERE status = 'DOWNLOADED' AND filePath IS NOT NULL")
	fun getDownloadedEntitiesFlow(): Flow<List<DownloadEntity>>

	@Query("SELECT filePath FROM DownloadEntity WHERE status = 'DOWNLOADED' AND filePath IS NOT NULL")
	fun getDownloadedFilePathsFlow(): Flow<List<String>>

	@Query("SELECT * FROM DownloadEntity WHERE songId IN (:songIds)")
	fun getDownloadsForSongsFlow(songIds: Collection<String>): Flow<List<DownloadEntity>>

	@Query("SELECT songId FROM DownloadEntity WHERE songId IN (:songIds) AND status IN ('DOWNLOADED', 'DOWNLOADING')")
	suspend fun getActiveOrCompletedDownloadIds(songIds: Collection<String>): List<String>

	@Query(
		"""
		SELECT COUNT(*) FROM DownloadEntity d
		INNER JOIN SongEntity s ON s.songId = d.songId
		WHERE d.status = 'DOWNLOADED' AND s.coverArtId = :coverArtId
		"""
	)
	suspend fun countDownloadedSongsWithCoverArt(coverArtId: String): Int
}
