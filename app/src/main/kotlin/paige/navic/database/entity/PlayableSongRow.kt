/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.entity

import paige.navic.domain.model.DomainSongArtist
import kotlin.time.Duration

data class PlayableSongRow(
	val songId: String,
	val title: String,
	val artistName: String?,
	val albumTitle: String?,
	val albumId: String?,
	val coverArtId: String?,
	val duration: Duration,
	val filePath: String?,
	val artists: List<DomainSongArtist>,
	val albumArtists: List<DomainSongArtist>
)
