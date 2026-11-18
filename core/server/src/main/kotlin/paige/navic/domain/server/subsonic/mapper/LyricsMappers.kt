/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.StructuredLyrics as ApiStructuredLyrics
import paige.navic.domain.model.ServerLyricLine
import paige.navic.domain.model.ServerLyrics

fun ApiStructuredLyrics.toDomainModel() = ServerLyrics(
	lang = lang,
	synced = synced,
	offsetMillis = offset,
	lines = lines.map { ServerLyricLine(startMillis = it.start?.toLong(), text = it.value) }
)
