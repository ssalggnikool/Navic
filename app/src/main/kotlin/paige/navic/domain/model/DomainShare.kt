/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import dev.zt64.subsonic.api.model.SubsonicResource as ApiSubsonicResource

@Immutable
data class DomainShare(
	val id: String,
	val url: String,
	val description: String?,
	val username: String,
	val createdAt: Instant,
	val expiresAt: Instant?,
	val lastVisited: Instant?,
	val visitCount: Int,
	val items: List<ApiSubsonicResource>
)
