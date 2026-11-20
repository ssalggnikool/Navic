/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.Album as ApiAlbum
import dev.zt64.subsonic.api.model.AlbumInfo as ApiAlbumInfo
import dev.zt64.subsonic.api.model.AlbumListType as ApiAlbumListType
import paige.navic.database.entity.AlbumEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainAlbumListType

fun ApiAlbum.toEntity(
	artistIdOverride: String? = null,
	artistNameOverride: String? = null
) = AlbumEntity(
	albumId = this.id,
	name = this.name,
	artistId = artistIdOverride ?: this.artistId ?: "unknown artist",
	artistName = artistNameOverride ?: this.artistName,
	coverArtId = this.coverArtId,
	songCount = this.songCount,
	duration = this.duration,
	year = this.year,
	genre = this.genre,
	starredAt = this.starredAt,
	userRating = this.userRating,
	musicBrainzId = this.musicBrainzId,
	createdAt = this.createdAt,
	lastPlayedAt = this.lastPlayedAt,
	playCount = this.playCount,
	genres = this.genres,
	version = this.version,
	isExternal = this.isExternal,
	displayArtist = this.displayArtist
)

fun ApiAlbum.toDomainModel(includeSongs: Boolean = false): DomainAlbum =
	toEntity().toDomainModel(
		songs = if (includeSongs) {
			songs.map { it.toEntity().toDomainModel() }
		} else {
			emptyList()
		}
	)

fun ApiAlbumInfo.toDomainModel() = DomainAlbumInfo(
	musicBrainzId = musicBrainzId,
	largeImageUrl = largeImageUrl,
	mediumImageUrl = mediumImageUrl,
	smallImageUrl = smallImageUrl,
	lastFmUrl = lastFmUrl,
	notes = notes
)

fun DomainAlbumListType.toApi(): ApiAlbumListType = when (this) {
	DomainAlbumListType.Random -> ApiAlbumListType.Random
	DomainAlbumListType.Newest -> ApiAlbumListType.Newest
	DomainAlbumListType.Highest -> ApiAlbumListType.Highest
	DomainAlbumListType.Frequent -> ApiAlbumListType.Frequent
	DomainAlbumListType.Recent -> ApiAlbumListType.Recent
	DomainAlbumListType.AlphabeticalByName -> ApiAlbumListType.AlphabeticalByName
	DomainAlbumListType.AlphabeticalByArtist -> ApiAlbumListType.AlphabeticalByArtist
	DomainAlbumListType.Year -> ApiAlbumListType.AlphabeticalByName
	is DomainAlbumListType.ByYear -> ApiAlbumListType.ByYear(fromYear, toYear)
	is DomainAlbumListType.ByGenre -> ApiAlbumListType.ByGenre(genre)
}
