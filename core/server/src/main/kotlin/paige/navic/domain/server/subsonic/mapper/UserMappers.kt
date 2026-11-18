/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.User as ApiUser
import paige.navic.domain.model.DomainUser

fun ApiUser.toDomainModel() = DomainUser(
	username = name,
	email = email,
	scrobblingEnabled = scrobblingEnabled,
	roles = roles.map { it.name.lowercase() }.toSet()
)
