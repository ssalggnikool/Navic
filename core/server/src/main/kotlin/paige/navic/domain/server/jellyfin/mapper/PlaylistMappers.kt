/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.jellyfin.mapper

import org.jellyfin.sdk.model.api.BaseItemDto
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

fun BaseItemDto.toDomainPlaylist(songs: List<DomainSong> = emptyList()): DomainPlaylist {
	val playlistId = id.toString()
	val durationMs = runTimeTicks?.let { it / 10_000L } ?: 0L

	return DomainPlaylist(
		id = playlistId,
		name = name ?: "Playlist",
		owner = null,
		comment = overview,
		coverArtId = playlistId,
		songCount = childCount ?: songs.size,
		duration = durationMs.milliseconds,
		createdAt = Instant.fromEpochMilliseconds(0),
		modifiedAt = Instant.fromEpochMilliseconds(0),
		public = true,
		readOnly = false,
		allowedUsers = emptyList(),
		validUntil = null,
		songs = songs
	)
}
