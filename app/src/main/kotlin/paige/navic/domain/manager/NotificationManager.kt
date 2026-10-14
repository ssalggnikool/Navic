/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import paige.navic.R
import android.app.NotificationManager as AndroidNotificationManager

object NotificationIds {
	const val DOWNLOAD_LIBRARY = 1001
	const val SYNC_LIBRARY = 1002
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
			val syncChannel = NotificationChannel(
				CHANNEL_SYNC_ID,
				"Library Synchronization",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Notifications for database synchronization updates"
			}

			val downloadChannel = NotificationChannel(
				CHANNEL_DOWNLOAD_ID,
				"Music Downloads",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Notifications for music file and media downloads"
			}

			notificationManager.createNotificationChannel(syncChannel)
			notificationManager.createNotificationChannel(downloadChannel)
		}
	}

	fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
		val channelId = if (id == NotificationIds.SYNC_LIBRARY) CHANNEL_SYNC_ID else CHANNEL_DOWNLOAD_ID
		val builder = NotificationCompat.Builder(context, channelId)
			.setSmallIcon(R.drawable.ic_navic)
			.setContentTitle(title)
			.setContentText(message)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setProgress(100, (progress * 100).toInt(), indeterminate)

		notificationManager.notify(id, builder.build())
	}

	fun cancelNotification(id: Int) {
		notificationManager.cancel(id)
	}

	companion object {
		private const val CHANNEL_SYNC_ID = "library_sync"
		private const val CHANNEL_DOWNLOAD_ID = "music_downloads"
	}
}
