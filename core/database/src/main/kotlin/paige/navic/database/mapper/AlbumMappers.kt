/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.AlbumEntity
import paige.navic.database.relation.AlbumWithSongs
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainSong

fun AlbumEntity.toDomainModel(songs: List<DomainSong> = emptyList()) = DomainAlbum(
	id = this.albumId,
	name = this.name,
	artistId = this.artistId,
	artistName = this.artistName,
	displayArtist = this.displayArtist,
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
	songs = songs,
	isExternal = this.isExternal
)

fun AlbumWithSongs.toDomainModel() = album.toDomainModel(
	songs = songs
		.map { it.toDomainModel() }
		.sortedBy { it.trackNumber }
)

fun DomainAlbum.toEntity(
	artistIdOverride: String? = null,
	artistNameOverride: String? = null
) = AlbumEntity(
	albumId = this.id,
	name = this.name,
	artistId = artistIdOverride ?: this.artistId,
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
