/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.relation.PlaylistWithSongs
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSong

fun PlaylistEntity.toDomainModel(songs: List<DomainSong> = emptyList()) = DomainPlaylist(
	id = this.playlistId,
	name = this.name,
	owner = this.owner,
	comment = this.comment,
	coverArtId = this.coverArtId,
	songCount = this.songCount,
	duration = this.duration,
	createdAt = this.createdAt,
	modifiedAt = this.modifiedAt,
	public = this.public,
	readOnly = this.readOnly,
	allowedUsers = this.allowedUsers,
	validUntil = this.validUntil,
	songs = songs
)

fun PlaylistWithSongs.toDomainModel() = playlist.toDomainModel(
	songs = songs
		.sortedBy { it.crossRef.position }
		.map { it.song.toDomainModel() }
)

fun DomainPlaylist.toEntity() = PlaylistEntity(
    playlistId = id,
    name = name,
    owner = owner,
    comment = comment,
    coverArtId = coverArtId,
    songCount = songCount,
    duration = duration,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    public = public,
    readOnly = readOnly,
    allowedUsers = allowedUsers,
    validUntil = validUntil
)
