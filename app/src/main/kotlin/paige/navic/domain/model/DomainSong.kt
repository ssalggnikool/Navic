/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

interface SongInfo {
	val id: String
	val albumTitle: String?
	val year: Int?
	val playCount: Int
	val isExternal: Boolean
	val artists: List<DomainSongArtist>
}

@Immutable
@Serializable
data class DomainSong(
	override val id: String,
	val title: String,
	val artistName: String?,
	val artistId: String,
	override val albumTitle: String?,
	val albumId: String?,
	val parentId: String?,
	val comment: String?,
	val trackNumber: Int?,
	val discNumber: Int?,
	val isrc: List<String>,
	override val year: Int?,
	val genre: String?,
	val genres: List<String>,
	val moods: List<String>,
	val duration: Duration,
	val bpm: Int?,
	val contributors: List<DomainContributor>,
	override val playCount: Int = 0,
	val userRating: Int?,
	val averageRating: Float?,
	val bitRate: Int?,
	val bitDepth: Int?,
	val sampleRate: Int?,
	val audioChannelCount: Int?,
	val replayGain: DomainReplayGain?,
	val fileSize: Long,
	val fileExtension: String?,
	val mimeType: String?,
	val filePath: String?,
	val starredAt: Instant?,
	val coverArtId: String?,
	val musicBrainzId: String?,
	val explicitStatus: DomainExplicitStatus,
	override val artists: List<DomainSongArtist>,
	val albumArtists: List<DomainSongArtist>,
	override val isExternal: Boolean = false
) : SongInfo
