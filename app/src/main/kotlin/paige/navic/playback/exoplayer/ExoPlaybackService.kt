/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer

import android.content.Intent
import androidx.annotation.OptIn
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
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.EqualizerManager
import paige.navic.domain.manager.NotificationIds
import paige.navic.domain.manager.NotificationStringIds
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.ScrobbleManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SyncManager
import paige.navic.playback.exoplayer.impl.playback.ExoEqualizerManager

@OptIn(UnstableApi::class)
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

	private lateinit var scrobbleManager: ScrobbleManager

	override fun onCreate() {
		super.onCreate()
		exoEqualizerManager = ExoEqualizerManager(
			equalizerManager,
			this
		)
		stateHolder.initState()

		val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
			.apply {
				setChannelId(NotificationStringIds.CHANNEL_MEDIA_SESSION_ID)
				setNotificationId(NotificationIds.MEDIA_SESSION)
				setChannelName(R.string.media_session_channel_name)
			}.build()
			.apply {
				setSmallIcon(R.drawable.ic_navic)
			}

		setMediaNotificationProvider(notificationProvider)

		stateHolder.playerInstance.let { player ->
			player as Player

			scrobbleManager = ScrobbleManager(
				player,
				connectivityManager,
				syncManager,
				sessionManager,
				preferenceManager
			)

			player.addListener(exoEqualizerManager)

			exoEqualizerManager.apply {
				applyEqualizerMode(equalizerManager.config.value.mode)
				scope.launch(Dispatchers.Main) {
					equalizerManager.config.collect { updateConfig() }
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
