/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.navidrome.api.domain

import kotlinx.serialization.Serializable

@Serializable
data class NavidromeCredentials(
	val username: String,
	val password: String
)
