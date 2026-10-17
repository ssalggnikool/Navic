/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.query

import androidx.room3.RoomRawQuery
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSongListType

fun DomainSongListType.toSqlQuery(
	reversed: Boolean = false,
	filters: Set<DomainFilter> = emptySet()
): RoomRawQuery {
	val whereClauses = mutableListOf<String>()
	val args = mutableListOf<Any>()

	when (this) {
		is DomainSongListType.ByArtist -> {
			whereClauses.add("artistId = ?")
			args.add(artistId)
		}

		is DomainSongListType.ByGenre -> {
			whereClauses.add("(genre = ? OR genres LIKE '%' || ? || '%')")
			args.add(genre)
			args.add(genre)
		}

		else -> {}
	}

	if (filters.contains(DomainFilter.Starred)) {
		whereClauses.add("starredAt IS NOT NULL")
	}

	if (filters.contains(DomainFilter.Downloaded)) {
		whereClauses.add(
			"songId IN (SELECT songId FROM DownloadEntity WHERE status = 'DOWNLOADED')",
		)
	}

	val whereSql = if (whereClauses.isNotEmpty()) {
		" WHERE " + whereClauses.joinToString(" AND ")
	} else {
		""
	}

	var orderBy = when (this) {
		DomainSongListType.FrequentlyPlayed -> "playCount DESC, LOWER(title) ASC"
		DomainSongListType.Newest -> "(SELECT AlbumEntity.createdAt FROM AlbumEntity WHERE AlbumEntity.albumId = SongEntity.belongsToAlbumId) DESC, LOWER(title) ASC"
		DomainSongListType.Random -> "RANDOM()"
		DomainSongListType.Rating -> "COALESCE(userRating, 0) DESC, LOWER(title) ASC"
		DomainSongListType.Year -> "COALESCE(year, 0) DESC, LOWER(title) ASC"
		is DomainSongListType.ByArtist -> "playCount DESC, LOWER(title) ASC"
		is DomainSongListType.ByGenre -> "playCount DESC, LOWER(title) ASC"
	}

	if (reversed && (this != DomainSongListType.Random)) {
		orderBy = orderBy.reverseOrder()
	}

	val sql = "SELECT * FROM SongEntity$whereSql ORDER BY $orderBy"

	return createRoomRawQuery(sql, args)
}
