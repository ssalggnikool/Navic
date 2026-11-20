/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.Share as ApiShare
import dev.zt64.subsonic.api.model.SubsonicResource as ApiSubsonicResource
import paige.navic.domain.model.DomainShare
import paige.navic.domain.model.DomainShareItem

fun ApiSubsonicResource.toDomainModel() = DomainShareItem(
	id = id,
	coverArtId = coverArtId
)

fun ApiShare.toDomainModel() = DomainShare(
	id = id,
	url = url,
	description = description,
	username = username,
	createdAt = createdAt,
	expiresAt = expiresAt,
	lastVisited = lastVisited,
	visitCount = visitCount,
	items = items.map { it.toDomainModel() }
)
