/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.exoplayer

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.R
import paige.navic.domain.manager.AndroidScrobbleManager
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.EqualizerManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SyncManager
import paige.navic.exoplayer.impl.ExoEqualizerManager

@UnstableApi
class ExoPlaybackService : MediaSessionService(), KoinComponent {
	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	private val serviceScope = MainScope()

	private val stateHolder: ExoStateHolder by inject()

	private val connectivityManager: ConnectivityManager by inject()

	private val syncManager: SyncManager by inject()
	private val sessionManager: SessionManager by inject()
	private val preferenceManager: PreferenceManager by inject()
	private val equalizerManager: EqualizerManager by inject()

	private lateinit var exoEqualizerManager: ExoEqualizerManager

	private lateinit var scrobbleManager: AndroidScrobbleManager

	override fun onCreate() {
		super.onCreate()
		exoEqualizerManager = ExoEqualizerManager(
            equalizerManager,
            this
        )
		stateHolder.initState()

		val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
			.build().apply {
				setSmallIcon(R.drawable.ic_navic)
			}

		setMediaNotificationProvider(notificationProvider)

		stateHolder.playerInstance.let { player ->
			player as Player

			scrobbleManager = AndroidScrobbleManager(
                player,
                serviceScope,
                connectivityManager,
                syncManager,
                sessionManager,
                preferenceManager
            )

			player.addListener(exoEqualizerManager)

			exoEqualizerManager.apply {
				applyEqualizerMode(equalizerManager.config.value.mode)
				scope.launch(Dispatchers.Main) {
					equalizerManager.config.collect { updateEqualizer() }
				}
			}
		}
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
		return stateHolder.mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		pauseAllPlayersAndStopSelf()
	}

	override fun onDestroy() {
		exoEqualizerManager.releaseEqualizer()
		scrobbleManager.release()
		serviceScope.cancel()
		stopForeground(STOP_FOREGROUND_REMOVE)
		stateHolder.destroySession()
		super.onDestroy()
		stopSelf()
	}
}
