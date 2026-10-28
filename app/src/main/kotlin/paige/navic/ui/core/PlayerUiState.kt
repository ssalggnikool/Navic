/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

import kotlinx.serialization.Serializable
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection

@Serializable
data class PlayerUiState(
	val queue: List<DomainSong> = emptyList(),
	val currentSong: DomainSong? = null,
	val currentCollection: DomainSongCollection? = null,
	val currentIndex: Int = -1,
	val isPaused: Boolean = false,
	val isShuffleEnabled: Boolean = false,
	val shuffleOrder: List<Int> = emptyList(),
	val repeatMode: Int = 0,
	val progress: Float = 0f,
	val isLoading: Boolean = false,
	val preservePitch: Boolean = true,
	val playbackSpeed: Float = 1.0f,
	val playbackBitrate: Int? = null,
	val playbackBitDepth: Int? = null,
	val playbackSampleRate: Int? = null,
	val playbackMimeType: String? = null
) {
	val isShuffled: Boolean
		get() = isShuffleEnabled && shuffleOrder.size == queue.size && shuffleOrder.isNotEmpty() &&
			shuffleOrder.all { it in queue.indices }

	val activeQueue: List<DomainSong>
		get() = if (isShuffled) shuffleOrder.map(queue::get) else queue

	val activeIndex: Int
		get() {
			if (!isShuffled) return currentIndex
			return shuffleOrder.indexOf(currentIndex).takeIf { it >= 0 } ?: currentIndex
		}
}
