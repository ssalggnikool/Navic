/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.repository.AlbumRepository
import paige.navic.domain.repository.ArtistRepository
import paige.navic.domain.repository.CollectionRepository
import paige.navic.domain.repository.DbRepository
import paige.navic.domain.repository.GenreRepository
import paige.navic.domain.repository.LyricsRepository
import paige.navic.domain.repository.PlayerStateRepository
import paige.navic.domain.repository.PlaylistRepository
import paige.navic.domain.repository.RadioRepository
import paige.navic.domain.repository.SearchRepository
import paige.navic.domain.repository.ShareRepository
import paige.navic.domain.repository.SongRepository

val repositoryModule = module {
	singleOf(::AlbumRepository)
	singleOf(::ArtistRepository)
	singleOf(::CollectionRepository)
	singleOf(::DbRepository)
	singleOf(::GenreRepository)
	singleOf(::LyricsRepository)
	singleOf(::PlayerStateRepository)
	singleOf(::PlaylistRepository)
	singleOf(::RadioRepository)
	singleOf(::SearchRepository)
	singleOf(::ShareRepository)
	singleOf(::SongRepository)
}
