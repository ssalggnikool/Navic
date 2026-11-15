/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Immutable
@Serializable
sealed interface DomainSongCollection {
	val id: String
	val name: String?
	val coverArtId: String?
	val duration: Duration?
	val songCount: Int
	val songs: List<DomainSong>
}
