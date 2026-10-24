/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import paige.navic.R
import android.app.NotificationManager as AndroidNotificationManager

object NotificationIds {
	const val MEDIA_SESSION = 1000
	const val DOWNLOAD_LIBRARY = 1001
	const val SYNC_LIBRARY = 1002
	const val DOWNLOAD_MIGRATION = 1003
}

object NotificationStringIds {
	const val CHANNEL_GROUP_MEDIA_PLAYBACK = "media_playback_group"
	const val CHANNEL_GROUP_BACKGROUND_ACTIVITIES = "background_activities_group"
	const val CHANNEL_MEDIA_SESSION_ID = "media_session"
	const val CHANNEL_SYNC_ID = "library_sync"
	const val CHANNEL_DOWNLOAD_ID = "music_downloads"
}

class NotificationManager(
	private val context: Context
) {
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

	init {
		createNotificationChannels()
	}

	private fun createNotificationChannels() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val mediaPlaybackGroup = NotificationChannelGroup(
				NotificationStringIds.CHANNEL_GROUP_MEDIA_PLAYBACK,
				"Playback"
			)

			val backgroundActivitiesGroup = NotificationChannelGroup(
				NotificationStringIds.CHANNEL_GROUP_BACKGROUND_ACTIVITIES,
				"Background activities"
			)

			val mediaSessionChannel = NotificationChannel(
				NotificationStringIds.CHANNEL_MEDIA_SESSION_ID,
				"Media controls",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Dedicated notification for media playback control"
				setSound(
					null, AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
						.build()
				)
				enableLights(false)
				enableVibration(false)
				group = NotificationStringIds.CHANNEL_GROUP_MEDIA_PLAYBACK
			}

			val syncChannel = NotificationChannel(
				NotificationStringIds.CHANNEL_SYNC_ID,
				"Library synchronization",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Notifications for database synchronization updates"
				group = NotificationStringIds.CHANNEL_GROUP_BACKGROUND_ACTIVITIES
			}

			val downloadChannel = NotificationChannel(
				NotificationStringIds.CHANNEL_DOWNLOAD_ID,
				"Music downloads",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Notifications for music file and media downloads"
				group = NotificationStringIds.CHANNEL_GROUP_BACKGROUND_ACTIVITIES
			}

			deleteOrphanNotificationChannels()

			notificationManager.apply {
				createNotificationChannelGroups(
					listOf(
						mediaPlaybackGroup,
						backgroundActivitiesGroup
					)
				)

				createNotificationChannels(
					listOf(
						mediaSessionChannel,
						syncChannel,
						downloadChannel
					)
				)
			}
		}
	}

	fun deleteOrphanNotificationChannels() {
		val currentChannels = listOf(
			NotificationStringIds.CHANNEL_SYNC_ID,
			NotificationStringIds.CHANNEL_MEDIA_SESSION_ID,
			NotificationStringIds.CHANNEL_DOWNLOAD_ID
		)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationManager.notificationChannels.forEach { channel ->
				val channelId = channel.id

				if (channelId in currentChannels) {
					return@forEach
				}

				notificationManager.deleteNotificationChannel(channelId)
			}
		}
	}

	fun createProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	): Notification {
		val channelId =
			if (id == NotificationIds.SYNC_LIBRARY) {
				NotificationStringIds.CHANNEL_SYNC_ID
			} else {
				NotificationStringIds.CHANNEL_DOWNLOAD_ID
			}
		return NotificationCompat.Builder(context, channelId)
			.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
			.setSmallIcon(R.drawable.ic_navic)
			.setContentTitle(title)
			.setContentText(message)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setProgress(100, (progress * 100).toInt(), indeterminate)
			.build()
	}

	fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
		notificationManager.notify(
			id,
			createProgressNotification(id, title, message, progress, indeterminate)
		)
	}

	fun cancelNotification(id: Int) {
		notificationManager.cancel(id)
	}
}
