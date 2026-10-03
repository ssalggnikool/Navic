/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import androidx.room3.RoomRawQuery
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType

fun ImmutableList<DomainSong>.sortedByListType(
	listType: DomainSongListType,
	albums: List<DomainAlbum>
): ImmutableList<DomainSong> {
	return when (listType) {
		DomainSongListType.FrequentlyPlayed -> sortedByDescending { it.playCount }
		DomainSongListType.Newest -> sortedByDescending {
			albums
				.firstOrNull { album -> album.id == it.albumId }
				?.createdAt
		}

		DomainSongListType.Random -> shuffled()

		DomainSongListType.Rating -> sortedByDescending { it.userRating ?: 0 }
		DomainSongListType.Year -> sortedByDescending { it.year }

		is DomainSongListType.ByArtist -> filter {
			it.artistId == listType.artistId
		}.sortedByDescending { it.playCount }

		is DomainSongListType.ByGenre -> filter {
			it.genre == listType.genre
				|| listType.genre in it.genres
		}.sortedByDescending { it.playCount }
	}.toImmutableList()
}

fun DomainAlbumListType.toSqlQuery(
	limit: Int? = null,
	offset: Int? = null,
	reversed: Boolean = false,
	filters: Set<DomainFilter> = emptySet()
): RoomRawQuery {
	val conditions = mutableListOf<String>()
	val args = mutableListOf<Any>()

	val orderBy = when (this) {
		DomainAlbumListType.AlphabeticalByArtist -> if (reversed) "LOWER(artistName) DESC" else "LOWER(artistName) ASC"
		DomainAlbumListType.AlphabeticalByName -> if (reversed) "LOWER(name) DESC" else "LOWER(name) ASC"
		DomainAlbumListType.Frequent -> {
			conditions.add("playCount != 0")
			if (reversed) "playCount ASC" else "playCount DESC"
		}

		DomainAlbumListType.Highest -> if (reversed) "userRating ASC" else "userRating DESC"
		DomainAlbumListType.Newest -> if (reversed) "createdAt ASC" else "createdAt DESC"
		DomainAlbumListType.Random -> "RANDOM()"
		DomainAlbumListType.Recent -> if (reversed) "lastPlayedAt ASC" else "lastPlayedAt DESC"

		DomainAlbumListType.Year -> if (reversed) "year ASC" else "year DESC"

		is DomainAlbumListType.ByGenre -> {
			conditions.add("genre = ?")
			args.add(genre)
			if (reversed) "LOWER(name) DESC" else "LOWER(name) ASC"
		}

		is DomainAlbumListType.ByYear -> {
			conditions.add("COALESCE(year, 0) BETWEEN ? AND ?")
			args.add(fromYear)
			args.add(toYear)
			if (reversed) "LOWER(name) DESC" else "LOWER(name) ASC"
		}
	}

	if (filters.contains(DomainFilter.Starred)) {
		conditions.add("starredAt IS NOT NULL")
	}

	val whereClause = if (conditions.isNotEmpty()) " WHERE ${conditions.joinToString(" AND ")}" else ""
	var sql = "SELECT * FROM AlbumEntity$whereClause ORDER BY $orderBy"

	if (limit != null && offset != null) {
		sql += " LIMIT ? OFFSET ?"
		args.add(limit)
		args.add(offset)
	}

	return RoomRawQuery(sql) { statement ->
		args.forEachIndexed { index, arg ->
			val bindIndex = index + 1
			when (arg) {
				is String -> statement.bindText(bindIndex, arg)
				is Int -> statement.bindInt(bindIndex, arg)
				is Long -> statement.bindLong(bindIndex, arg)
				is Float -> statement.bindFloat(bindIndex, arg)
				is Double -> statement.bindDouble(bindIndex, arg)
				is Boolean -> statement.bindInt(bindIndex, if (arg) 1 else 0)
			}
		}
	}
}
