/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

import kotlinx.serialization.Serializable
import paige.navic.domain.model.DomainExplicitStatus
import paige.navic.domain.model.DomainReplayGain
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongArtist
import paige.navic.domain.model.SongInfo
import kotlin.time.Duration

@Serializable
data class QueueSong(
	override val id: String,
	val title: String,
	val artistName: String?,
	override val albumTitle: String?,
	val albumId: String?,
	override val artists: List<DomainSongArtist>,
	val albumArtists: List<DomainSongArtist>,
	val duration: Duration,
	val coverArtId: String?,
	val filePath: String?,
	override val year: Int?,
	override val playCount: Int = 0,
	override val isExternal: Boolean = false,
	val explicitStatus: DomainExplicitStatus,
	val replayGain: DomainReplayGain? = null
) : SongInfo

fun DomainSong.toQueueSong() = QueueSong(
	id = id,
	title = title,
	artistName = artistName,
	albumTitle = albumTitle,
	albumId = albumId,
	artists = artists,
	albumArtists = albumArtists,
	duration = duration,
	coverArtId = coverArtId,
	filePath = filePath,
	year = year,
	playCount = playCount,
	isExternal = isExternal,
	explicitStatus = explicitStatus,
	replayGain = replayGain
)


fun QueueSong.toDomainSong() = DomainSong(
	id = id,
	title = title,
	artistName = artistName,
	artistId = "",
	albumTitle = albumTitle,
	albumId = albumId,
	parentId = null,
	comment = null,
	trackNumber = null,
	discNumber = null,
	isrc = emptyList(),
	year = year,
	genre = null,
	genres = emptyList(),
	moods = emptyList(),
	duration = duration,
	bpm = null,
	contributors = emptyList(),
	playCount = playCount,
	userRating = null,
	averageRating = null,
	bitRate = null,
	bitDepth = null,
	sampleRate = null,
	audioChannelCount = null,
	replayGain = replayGain,
	fileSize = 0,
	fileExtension = null,
	mimeType = null,
	filePath = filePath,
	starredAt = null,
	coverArtId = coverArtId,
	musicBrainzId = null,
	explicitStatus = explicitStatus,
	artists = artists,
	albumArtists = albumArtists,
	isExternal = isExternal
)
