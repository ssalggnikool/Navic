/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class DomainChatMessage(
	val username: String,
	val time: Instant,
	val message: String
)
