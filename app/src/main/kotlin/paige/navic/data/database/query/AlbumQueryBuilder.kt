/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.query

import androidx.room3.RoomRawQuery
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter

fun DomainAlbumListType.toSqlQuery(
	reversed: Boolean = false,
	filters: Set<DomainFilter> = emptySet()
): RoomRawQuery {
	val whereClauses = mutableListOf<String>()
	val args = mutableListOf<Any>()

	when (this) {
		DomainAlbumListType.AlphabeticalByArtist,
		DomainAlbumListType.AlphabeticalByName,
		DomainAlbumListType.Highest,
		DomainAlbumListType.Newest,
		DomainAlbumListType.Random,
		DomainAlbumListType.Recent,
		DomainAlbumListType.Year -> {}

		DomainAlbumListType.Frequent -> {
			whereClauses.add("playCount != 0")
		}

		is DomainAlbumListType.ByGenre -> {
			whereClauses.add("(genre = ? OR genres LIKE '%' || ? || '%')")
			args.add(genre)
			args.add(genre)
		}

		is DomainAlbumListType.ByYear -> {
			whereClauses.add("COALESCE(year, 0) BETWEEN ? AND ?")
			args.add(fromYear)
			args.add(toYear)
		}
	}

	if (filters.contains(DomainFilter.Starred)) {
		whereClauses.add("starredAt IS NOT NULL")
	}

	if (filters.contains(DomainFilter.Downloaded)) {
		whereClauses.add(
			"NOT EXISTS (" +
				"SELECT 1 FROM SongEntity s " +
				"WHERE s.belongsToAlbumId = AlbumEntity.albumId " +
				"AND s.songId NOT IN (SELECT songId FROM DownloadEntity WHERE status = 'DOWNLOADED')" +
				")",
		)
	}

	val whereSql = if (whereClauses.isNotEmpty()) {
		" WHERE " + whereClauses.joinToString(" AND ")
	} else {
		""
	}

	var orderBy = when (this) {
		DomainAlbumListType.AlphabeticalByArtist -> "LOWER(artistName) ASC, LOWER(name) ASC"
		DomainAlbumListType.AlphabeticalByName -> "LOWER(name) ASC"
		DomainAlbumListType.Frequent -> "playCount DESC, LOWER(name) ASC"
		DomainAlbumListType.Highest -> "userRating DESC, LOWER(name) ASC"
		DomainAlbumListType.Newest -> "createdAt DESC, LOWER(name) ASC"
		DomainAlbumListType.Random -> "RANDOM()"
		DomainAlbumListType.Recent -> "lastPlayedAt DESC, LOWER(name) ASC"
		DomainAlbumListType.Year -> "year DESC, LOWER(name) ASC"
		is DomainAlbumListType.ByGenre -> "LOWER(name) ASC"
		is DomainAlbumListType.ByYear -> "LOWER(name) ASC"
	}

	if (reversed && (this != DomainAlbumListType.Random)) {
		orderBy = orderBy.reverseOrder()
	}

	val sql = "SELECT * FROM AlbumEntity$whereSql ORDER BY $orderBy"

	return createRoomRawQuery(sql, args)
}
