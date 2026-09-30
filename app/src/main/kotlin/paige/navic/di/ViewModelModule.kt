/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import androidx.media3.common.util.UnstableApi
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType
import paige.navic.shared.AndroidMediaPlayerViewModel
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.dialog.DeletionViewModel
import paige.navic.ui.component.sheet.ChangelogViewModel
import paige.navic.ui.screen.album.viewmodel.AlbumListViewModel
import paige.navic.ui.screen.artist.viewmodel.ArtistDetailViewModel
import paige.navic.ui.screen.artist.viewmodel.ArtistListViewModel
import paige.navic.ui.screen.chat.viewmodel.ChatViewModel
import paige.navic.ui.screen.collection.viewmodel.CollectionDetailViewModel
import paige.navic.ui.screen.genre.viewmodel.GenreListViewModel
import paige.navic.ui.screen.lyrics.viewmodel.LyricsScreenViewModel
import paige.navic.ui.screen.nowPlaying.viewmodel.NowPlayingViewModel
import paige.navic.ui.screen.playlist.viewmodel.PlaylistCreateDialogViewModel
import paige.navic.ui.screen.playlist.viewmodel.PlaylistListViewModel
import paige.navic.ui.screen.playlist.viewmodel.PlaylistUpdateDialogViewModel
import paige.navic.ui.screen.queue.viewmodel.QueueViewModel
import paige.navic.ui.screen.radio.viewmodel.RadioCreateDialogViewModel
import paige.navic.ui.screen.radio.viewmodel.RadioListViewModel
import paige.navic.ui.screen.search.viewmodel.SearchViewModel
import paige.navic.ui.screen.settings.viewmodel.LyricsPriorityViewModel
import paige.navic.ui.screen.settings.viewmodel.NavtabsViewModel
import paige.navic.ui.screen.settings.viewmodel.SettingsDataStorageViewModel
import paige.navic.ui.screen.share.viewmodel.ShareDialogViewModel
import paige.navic.ui.screen.share.viewmodel.ShareListViewModel
import paige.navic.ui.screen.song.viewmodel.SongDetailViewModel
import paige.navic.ui.screen.song.viewmodel.SongListViewModel
import paige.navic.ui.screen.stats.viewmodel.StatisticsViewModel
import paige.navic.ui.viewmodel.RootViewModel

@UnstableApi
val viewModelModule = module {
	single<MediaPlayerViewModel> {
		AndroidMediaPlayerViewModel(
			application = androidApplication(),
			stateRepository = get(),
			songRepository = get(),
			albumDao = get(),
			downloadManager = get(),
			connectivityManager = get(),
			sessionManager = get(),
			preferenceManager = get(),
			snackBarManager = get(),
			audioGainManager = get(),
		)
	}

	viewModel { (artistId: String) ->
		ArtistDetailViewModel(
			artistId = artistId,
			repository = get(),
			artistRepository = get(),
			songRepository = get(),
			albumRepository = get(),
			artistDao = get(),
			albumDao = get(),
			downloadManager = get(),
			snackBarManager = get(),
			connectivityManager = get()
		)
	}

	viewModel { (song: DomainSong?) ->
		LyricsScreenViewModel(
			song = song,
			repository = get()
		)
	}

	viewModel { (songs: List<DomainSong>, playlistToExclude: String?) ->
		PlaylistUpdateDialogViewModel(
			songs = songs,
			playlistToExclude = playlistToExclude,
			sessionManager = get(),
			snackBarManager = get()
		)
	}

	viewModel { params ->
		AlbumListViewModel(
			initialListType = params.getOrNull<DomainAlbumListType>(),
			initialFilters = params.getOrNull<Set<DomainFilter>>(),
			repository = get(),
			sessionManager = get(),
			preferenceManager = get()
		)
	}
	viewModel { params ->
		SongListViewModel(
			initialListType = params.getOrNull<DomainSongListType>(),
			initialFilters = params.getOrNull<Set<DomainFilter>>(),
			repository = get(),
			downloadManager = get(),
			sessionManager = get(),
			preferenceManager = get(),
			connectivityManager = get()
		)
	}
	viewModel { params ->
		ArtistListViewModel(
			initialListType = params.getOrNull<DomainArtistListType>(),
			initialFilters = params.getOrNull<Set<DomainFilter>>(),
			repository = get(),
			albumDao = get(),
			sessionManager = get(),
			preferenceManager = get()
		)
	}
	viewModelOf(::SearchViewModel)
	viewModelOf(::GenreListViewModel)
	viewModelOf(::RadioListViewModel)
	viewModelOf(::RadioCreateDialogViewModel)
	viewModelOf(::PlaylistListViewModel)
	viewModelOf(::QueueViewModel)
	viewModelOf(::ShareListViewModel)
	viewModelOf(::DeletionViewModel)
	viewModelOf(::ShareDialogViewModel)
	viewModel { (songs: List<DomainSong>) ->
		PlaylistCreateDialogViewModel(
			songs = songs,
			playlistDao = get(),
			sessionManager = get(),
			snackBarManager = get()
		)
	}
	viewModel { params ->
		CollectionDetailViewModel(
			collectionId = params.get(),
			repository = get(),
			songRepository = get(),
			albumRepository = get(),
			downloadManager = get(),
			sessionManager = get(),
			snackBarManager = get(),
			connectivityManager = get()
		)
	}
	viewModelOf(::SongDetailViewModel)
	viewModelOf(::SettingsDataStorageViewModel)
	viewModelOf(::ChangelogViewModel)
	viewModel { params ->
		NowPlayingViewModel(
			player = params.get(),
			songRepository = get()
		)
	}
	viewModelOf(::NavtabsViewModel)
	viewModelOf(::LyricsPriorityViewModel)
	viewModelOf(::RootViewModel)
	viewModelOf(::StatisticsViewModel)
	viewModelOf(::ChatViewModel)
}
