/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.RewriteQueriesToDropUnusedColumns
import androidx.room3.RoomDatabase
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.ArtistDao
import paige.navic.database.dao.DownloadDao
import paige.navic.database.dao.GenreDao
import paige.navic.database.dao.LyricDao
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.RadioDao
import paige.navic.database.dao.SongDao
import paige.navic.database.dao.SyncActionDao
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.entity.ArtistEntity
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.GenreEntity
import paige.navic.database.entity.LyricEntity
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.entity.PlaylistSongCrossRef
import paige.navic.database.entity.RadioEntity
import paige.navic.database.entity.SongEntity
import paige.navic.database.entity.SyncActionEntity

@Database(
	version = 25,
	entities = [
		AlbumEntity::class,
		ArtistEntity::class,
		DownloadEntity::class,
		GenreEntity::class,
		LyricEntity::class,
		PlaylistEntity::class,
		PlaylistSongCrossRef::class,
		RadioEntity::class,
		SongEntity::class,
		SyncActionEntity::class
	],
	autoMigrations = [
		AutoMigration(from = 24, to = 25),
	]
)
@ColumnTypeConverters(Converters::class)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
@RewriteQueriesToDropUnusedColumns
abstract class CacheDatabase : RoomDatabase() {
	abstract fun albumDao(): AlbumDao
	abstract fun artistDao(): ArtistDao
	abstract fun downloadDao(): DownloadDao
	abstract fun genreDao(): GenreDao
	abstract fun lyricDao(): LyricDao
	abstract fun playlistDao(): PlaylistDao
	abstract fun radioDao(): RadioDao
	abstract fun songDao(): SongDao
	abstract fun syncActionDao(): SyncActionDao
}
