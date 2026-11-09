/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DomainUser(
	val username: String,
	val email: String? = null,
	val scrobblingEnabled: Boolean = true,
	val roles: Set<String> = emptySet()
)
