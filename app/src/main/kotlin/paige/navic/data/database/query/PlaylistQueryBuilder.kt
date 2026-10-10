/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.query

import androidx.room3.RoomRawQuery
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainPlaylistListType

fun DomainPlaylistListType.toSqlQuery(
	reversed: Boolean = false,
	filters: Set<DomainFilter> = emptySet()
): RoomRawQuery {
	val whereClauses = mutableListOf<String>()
	val args = mutableListOf<Any>()

	if (filters.contains(DomainFilter.Downloaded)) {
		whereClauses.add(
			"EXISTS (SELECT 1 FROM PlaylistSongCrossRef ref WHERE ref.playlistId = PlaylistEntity.playlistId) " +
				"AND NOT EXISTS (" +
				"SELECT 1 FROM PlaylistSongCrossRef ref " +
				"WHERE ref.playlistId = PlaylistEntity.playlistId " +
				"AND ref.songId NOT IN (SELECT songId FROM DownloadEntity WHERE status = 'DOWNLOADED')" +
				")",
		)
	}

	val whereSql = if (whereClauses.isNotEmpty()) {
		" WHERE " + whereClauses.joinToString(" AND ")
	} else {
		""
	}

	var orderBy = when (this) {
		DomainPlaylistListType.Name -> "name COLLATE NOCASE ASC"
		DomainPlaylistListType.DateAdded -> "createdAt DESC"
		DomainPlaylistListType.Duration -> "duration DESC"
		DomainPlaylistListType.Random -> "RANDOM()"
	}

	if (reversed && (this != DomainPlaylistListType.Random)) {
		orderBy = orderBy.reverseOrder()
	}

	val sql = "SELECT * FROM PlaylistEntity$whereSql ORDER BY $orderBy"

	return createRoomRawQuery(sql, args)
}
