/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server.subsonic.mapper

import dev.zt64.subsonic.api.model.SearchResult as ApiSearchResult
import paige.navic.domain.model.DomainSearchResult

fun ApiSearchResult.toDomainModel() = DomainSearchResult(
	albums = albums.map { it.toDomainModel() },
	artists = artists.map { it.toDomainModel() },
	songs = songs.map { it.toDomainModel() }
)
