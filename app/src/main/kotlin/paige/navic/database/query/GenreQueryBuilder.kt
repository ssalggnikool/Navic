/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.query

import androidx.room3.RoomRawQuery

fun toGenreSqlQuery(): RoomRawQuery {
	val sql = "SELECT * FROM GenreEntity WHERE EXISTS (" +
		"SELECT 1 FROM AlbumEntity " +
		"WHERE AlbumEntity.genre = GenreEntity.genreName " +
		"OR AlbumEntity.genres LIKE '%' || GenreEntity.genreName || '%'" +
		") ORDER BY albumCount DESC"
	return createRoomRawQuery(sql, emptyList())
}
