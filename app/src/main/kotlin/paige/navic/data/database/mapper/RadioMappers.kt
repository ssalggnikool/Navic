/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.mapper

import paige.navic.data.database.entity.RadioEntity
import paige.navic.domain.model.DomainRadio
import dev.zt64.subsonic.api.model.InternetRadioStation as ApiRadio

fun ApiRadio.toEntity() = RadioEntity(
	radioId = id,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)

fun RadioEntity.toDomainModel() = DomainRadio(
	id = radioId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)
