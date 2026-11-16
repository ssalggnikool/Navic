/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

import androidx.compose.runtime.Immutable
import paige.navic.domain.model.DomainSong

@Immutable
data class SavedQueueSnapshot(
	val songs: List<QueueSong>,
	val activeIndex: Int,
	val progress: Float,
	val isPaused: Boolean,
	val shuffleOrder: List<Int>,
	val isShuffleEnabled: Boolean,
	val currentSong: DomainSong?
)
