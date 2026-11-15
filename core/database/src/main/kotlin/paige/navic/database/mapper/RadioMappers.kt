/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.mapper

import paige.navic.database.entity.RadioEntity
import paige.navic.domain.model.DomainRadio

fun RadioEntity.toDomainModel() = DomainRadio(
	id = radioId,
	coverArtId = coverArtId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)

fun DomainRadio.toEntity() = RadioEntity(
	radioId = id,
	coverArtId = coverArtId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)
