package paige.navic.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.manager.AppIconManager
import paige.navic.domain.manager.AudioGainManager
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.EqualizerManager
import paige.navic.domain.manager.LinkManager
import paige.navic.domain.manager.LogManager
import paige.navic.domain.manager.LoginManager
import paige.navic.domain.manager.NotificationManager
import paige.navic.domain.manager.PermissionManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.ShareManager
import paige.navic.domain.manager.SleepTimerManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.manager.StorageManager
import paige.navic.domain.manager.SyncManager
import paige.navic.exoplayer.ExoStateHolder

val managerModule = module {
	singleOf(::SleepTimerManager)
	single(createdAtStart = true) {
		SyncManager(get(), get(), get(), get(), get(), get()).apply {
			startPeriodicSync()
		}
	}
	singleOf(::DownloadManager)
	singleOf(::SessionManager)
	singleOf(::PreferenceManager)
	singleOf(::SnackBarManager)
	singleOf(::LoginManager)
	singleOf(::EqualizerManager)
	singleOf(::ShareManager)
	singleOf(::NotificationManager)
	singleOf(::StorageManager)
	singleOf(::ConnectivityManager)
	singleOf(::LogManager)
	singleOf(::AppIconManager)
	singleOf(::PermissionManager)
	singleOf(::LinkManager)
	singleOf(::ExoStateHolder)
	singleOf(::AudioGainManager)
}
