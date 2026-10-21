/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.query

import androidx.room3.RoomRawQuery
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter

fun DomainArtistListType.toSqlQuery(
	reversed: Boolean = false,
	filters: Set<DomainFilter> = emptySet(),
	idsOnly: Boolean = false
): RoomRawQuery {
	val whereClauses = mutableListOf<String>()
	val args = mutableListOf<Any>()

	when (this) {
		DomainArtistListType.AlphabeticalByName,
		DomainArtistListType.Random,
		-> {}
	}

	if (filters.contains(DomainFilter.Starred)) {
		whereClauses.add("starredAt IS NOT NULL")
	}

	if (filters.contains(DomainFilter.Downloaded)) {
		whereClauses.add(
			"EXISTS (SELECT 1 FROM SongEntity s WHERE s.artistId = ArtistEntity.artistId) " +
				"AND NOT EXISTS (" +
				"SELECT 1 FROM SongEntity s " +
				"WHERE s.artistId = ArtistEntity.artistId " +
				"AND s.songId NOT IN (SELECT songId FROM DownloadEntity WHERE status = 'DOWNLOADED')" +
				")",
		)
	}

	val whereSql = if (whereClauses.isNotEmpty()) {
		" WHERE " + whereClauses.joinToString(" AND ")
	} else {
		""
	}

	if (idsOnly) {
		val sql = "SELECT artistId FROM ArtistEntity$whereSql"
		return createRoomRawQuery(sql, args)
	}

	var orderBy = when (this) {
		DomainArtistListType.AlphabeticalByName -> "name COLLATE NOCASE ASC"
		DomainArtistListType.Random -> "RANDOM()"
	}

	if (reversed && (this != DomainArtistListType.Random)) {
		orderBy = orderBy.reverseOrder()
	}

	val sql = "SELECT * FROM ArtistEntity$whereSql ORDER BY $orderBy"

	return createRoomRawQuery(sql, args)
}

fun getArtistLetterCountsQuery(
	filters: Set<DomainFilter> = emptySet()
): RoomRawQuery {
	val whereClauses = mutableListOf<String>()
	val args = mutableListOf<Any>()

	if (filters.contains(DomainFilter.Starred)) {
		whereClauses.add("starredAt IS NOT NULL")
	}

	if (filters.contains(DomainFilter.Downloaded)) {
		whereClauses.add(
			"EXISTS (SELECT 1 FROM SongEntity s WHERE s.artistId = ArtistEntity.artistId) " +
				"AND NOT EXISTS (" +
				"SELECT 1 FROM SongEntity s " +
				"WHERE s.artistId = ArtistEntity.artistId " +
				"AND s.songId NOT IN (SELECT songId FROM DownloadEntity WHERE status = 'DOWNLOADED')" +
				")",
		)
	}

	val whereSql = if (whereClauses.isNotEmpty()) {
		" WHERE " + whereClauses.joinToString(" AND ")
	} else {
		""
	}

	val sql = "SELECT CASE WHEN UPPER(SUBSTR(name, 1, 1)) BETWEEN 'A' AND 'Z' THEN UPPER(SUBSTR(name, 1, 1)) ELSE '#' END AS letter, COUNT(*) AS count FROM ArtistEntity$whereSql GROUP BY letter ORDER BY letter COLLATE NOCASE ASC"

	return createRoomRawQuery(sql, args)
}
