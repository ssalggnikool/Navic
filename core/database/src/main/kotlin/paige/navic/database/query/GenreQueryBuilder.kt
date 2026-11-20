/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.query

import androidx.room3.RoomRawQuery

fun toGenreSqlQuery(): RoomRawQuery {
	val albumMatch =
		"(AlbumEntity.genre = GenreEntity.genreName " +
			"OR AlbumEntity.genres LIKE '%' || GenreEntity.genreName || '%')"
	val sql = "SELECT GenreEntity.*, " +
		"(SELECT AlbumEntity.coverArtId FROM AlbumEntity " +
		"WHERE $albumMatch AND AlbumEntity.coverArtId IS NOT NULL LIMIT 1) AS firstCoverArtId, " +
		"(SELECT AlbumEntity.coverArtId FROM AlbumEntity " +
		"WHERE $albumMatch AND AlbumEntity.coverArtId IS NOT NULL LIMIT 1 OFFSET 1) AS secondCoverArtId " +
		"FROM GenreEntity WHERE EXISTS (" +
		"SELECT 1 FROM AlbumEntity WHERE $albumMatch" +
		") ORDER BY albumCount DESC"
	return createRoomRawQuery(sql, emptyList())
}
