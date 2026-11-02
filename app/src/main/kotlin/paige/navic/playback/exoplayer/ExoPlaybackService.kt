/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.R
import paige.navic.domain.manager.runtime.NotificationIds
import paige.navic.domain.manager.runtime.NotificationStringIds

@OptIn(UnstableApi::class)
class ExoPlaybackService : MediaLibraryService(), KoinComponent {
	private val stateHolder: ExoStateHolder by inject()

	override fun onCreate() {
		super.onCreate()
		stateHolder.onSessionReleased = { session ->
			if (session in getSessions()) removeSession(session)
		}
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
	}

	override fun onGetSession(
		controllerInfo: MediaSession.ControllerInfo
	): MediaLibrarySession {
		return stateHolder.mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		pauseAllPlayersAndStopSelf()
	}

	override fun onDestroy() {
		stopForeground(STOP_FOREGROUND_REMOVE)
		stateHolder.destroySession()
		super.onDestroy()
		stopSelf()
	}
}
