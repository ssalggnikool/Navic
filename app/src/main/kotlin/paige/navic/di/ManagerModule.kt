/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.download.DownloadProcessor
import paige.navic.domain.manager.playback.AudioGainManager
import paige.navic.domain.manager.playback.EqualizerManager
import paige.navic.domain.manager.playback.SleepTimerManager
import paige.navic.domain.manager.playback.StreamCacheManager
import paige.navic.domain.manager.runtime.NotificationManager
import paige.navic.domain.manager.runtime.PermissionManager
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.runtime.StorageManager
import paige.navic.domain.manager.session.ConnectivityManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.session.LoginManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.ui.AppIconManager
import paige.navic.domain.manager.ui.LinkManager
import paige.navic.domain.manager.ui.LogManager
import paige.navic.domain.manager.ui.ShareManager
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.domain.sync.SyncActionProcessor
import paige.navic.domain.sync.SyncCoordinator
import paige.navic.playback.exoplayer.ExoStateHolder
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource.NavicDataSourceFactory

val managerModule = module {
	singleOf(::AppIconManager)
	singleOf(::AudioGainManager)
	singleOf(::ConnectivityManager)
	singleOf(::DownloadManager)
	singleOf(::DownloadProcessor)
	singleOf(::EqualizerManager)
	singleOf(::LinkManager)
	singleOf(::LogManager)
	singleOf(::LoginManager)
	singleOf(::NotificationManager)
	singleOf(::PermissionManager)
	singleOf(::PreferenceManager)
	singleOf(::SessionManager)
	singleOf(::ShareManager)
	singleOf(::SleepTimerManager)
	singleOf(::SnackBarManager)
	singleOf(::StorageManager)
	singleOf(::StreamCacheManager)
	singleOf(::SyncActionProcessor)
	singleOf(::SyncCoordinator)
	single(createdAtStart = true) {
		SyncManager(get(), get(), get(), get(), get())
	}
	single {
		NavicDataSourceFactory(
			downloadManager = get(),
			sessionManager = get(),
			streamCacheManager = get(),
			preferenceManager = get(),
			dnsResolver = get()
		)
	}
	singleOf(::ExoStateHolder)
}
