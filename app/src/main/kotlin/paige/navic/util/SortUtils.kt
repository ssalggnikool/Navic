/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType

// TODO: sort with sql instead
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
