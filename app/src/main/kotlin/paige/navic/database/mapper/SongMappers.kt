/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.SongEntity
import paige.navic.domain.model.DomainSong

fun SongEntity.toDomainModel() = DomainSong(
	id = this.songId,
	title = this.title,
	artistName = this.artistName,
	artistId = this.artistId,
	albumTitle = this.albumTitle,
	albumId = this.belongsToAlbumId,
	coverArtId = this.coverArtId,
	duration = this.duration,
	trackNumber = this.trackNumber,
	discNumber = this.discNumber,
	year = this.year,
	genre = this.genre,
	bitRate = this.bitRate,
	mimeType = this.mimeType,
	fileExtension = this.fileExtension,
	filePath = this.filePath,
	starredAt = this.starredAt,
	parentId = this.parentId,
	genres = this.genres,
	moods = this.moods,
	isrc = this.isrc,
	bpm = this.bpm,
	comment = this.comment,
	playCount = this.playCount,
	userRating = this.userRating,
	averageRating = this.averageRating,
	bitDepth = this.bitDepth,
	sampleRate = this.sampleRate,
	audioChannelCount = this.audioChannelCount,
	fileSize = this.fileSize,
	musicBrainzId = this.musicBrainzId,
	contributors = this.contributors,
	replayGain = this.replayGain,
	explicitStatus = this.explicitStatus,
	artists = this.artists,
	albumArtists = this.albumArtists,
	isExternal = this.isExternal
)

fun DomainSong.toEntity(
	artistIdOverride: String? = null,
	artistNameOverride: String? = null
) = SongEntity(
    songId = this.id,
    title = this.title,
    artistName = artistNameOverride ?: this.artistName,
    artistId = artistIdOverride ?: this.artistId,
    albumTitle = this.albumTitle,
    belongsToAlbumId = this.albumId,
    coverArtId = this.coverArtId,
    duration = this.duration,
    trackNumber = this.trackNumber,
    discNumber = this.discNumber,
    year = this.year,
    genre = this.genre,
    bitRate = this.bitRate,
    mimeType = this.mimeType,
    fileExtension = this.fileExtension,
    filePath = this.filePath,
    starredAt = this.starredAt,
    parentId = this.parentId,
    genres = this.genres,
    moods = this.moods,
    isrc = this.isrc,
    bpm = this.bpm,
    comment = this.comment,
    playCount = this.playCount,
    userRating = this.userRating,
    averageRating = this.averageRating,
    bitDepth = this.bitDepth,
    sampleRate = this.sampleRate,
    audioChannelCount = this.audioChannelCount,
    fileSize = this.fileSize,
    musicBrainzId = this.musicBrainzId,
    replayGain = this.replayGain,
    contributors = this.contributors,
    explicitStatus = this.explicitStatus,
    artists = this.artists,
    albumArtists = this.albumArtists,
    isExternal = this.isExternal
)
