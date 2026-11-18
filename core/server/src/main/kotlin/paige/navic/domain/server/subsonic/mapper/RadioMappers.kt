/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.InternetRadioStation as ApiRadio
import paige.navic.database.entity.RadioEntity
import paige.navic.database.mapper.toDomainModel

fun ApiRadio.toEntity() = RadioEntity(
	radioId = id,
	coverArtId = coverArtId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)

fun ApiRadio.toDomainModel() = toEntity().toDomainModel()
