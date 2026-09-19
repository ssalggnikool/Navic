package paige.navic.domain.manager

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import paige.navic.di.ActivityProvider
import paige.navic.di.ResourceProvider

actual class NotificationManager(
	private val context: Context,
	private val resourceProvider: ResourceProvider,
	private val activityProvider: ActivityProvider
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

	actual fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
		val channelId = if (id == NotificationIds.SYNC_LIBRARY) CHANNEL_SYNC_ID else CHANNEL_DOWNLOAD_ID
		val builder = NotificationCompat.Builder(context, channelId)
			.setSmallIcon(resourceProvider.icNavic)
			.setContentTitle(title)
			.setContentText(message)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setProgress(100, (progress * 100).toInt(), indeterminate)

		notificationManager.notify(id, builder.build())
	}

	actual fun cancelNotification(id: Int) {
		notificationManager.cancel(id)
	}

	actual fun requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(
					context,
					Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				activityProvider.get<Activity>().let { activity ->
					ActivityCompat.requestPermissions(
						activity,
						arrayOf(Manifest.permission.POST_NOTIFICATIONS),
						101
					)
				}
			}
		}
	}

	companion object {
		private const val CHANNEL_SYNC_ID = "library_sync"
		private const val CHANNEL_DOWNLOAD_ID = "music_downloads"
	}
}
