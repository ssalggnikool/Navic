/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.query

import androidx.room3.RoomRawQuery

fun toAlbumByIdSqlQuery(albumId: String): RoomRawQuery {
	return createRoomRawQuery("SELECT * FROM AlbumEntity WHERE albumId = ? LIMIT 1", listOf(albumId))
}

fun toPlaylistByIdSqlQuery(playlistId: String): RoomRawQuery {
	return createRoomRawQuery("SELECT * FROM PlaylistEntity WHERE playlistId = ? LIMIT 1", listOf(playlistId))
}
