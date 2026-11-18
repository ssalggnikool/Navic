/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist
import paige.navic.database.entity.PlaylistEntity
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainPlaylist

fun ApiPlaylist.toEntity() = PlaylistEntity(
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

fun ApiPlaylist.toDomainModel(includeSongs: Boolean = false): DomainPlaylist =
	toEntity().toDomainModel(
		songs = if (includeSongs) {
			songs.map { it.toEntity().toDomainModel() }
		} else {
			emptyList()
		}
	)
