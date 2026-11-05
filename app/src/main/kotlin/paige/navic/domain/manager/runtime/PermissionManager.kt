/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.runtime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import paige.navic.di.ActivityProvider
import java.util.UUID
import kotlin.coroutines.resume

class PermissionManager(
	private val context: Context,
	private val activities: ActivityProvider
) {
	private val requestMutex = Mutex()

	fun openPermissionsSettings() {
		val activity = activities.get<ComponentActivity>()
		val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
			data = Uri.fromParts("package", activity.packageName, null)
		}
		activity.startActivity(intent)
	}

	suspend fun requestLocalNetworkPermission(): Boolean {
		// this permission is only needed on android 17
		if (Build.VERSION.SDK_INT < 37) return true
		return requestPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
	}

	suspend fun requestNotificationsPermission(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
		return requestPermission(Manifest.permission.POST_NOTIFICATIONS)
	}

	private fun isGranted(permission: String): Boolean =
		ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

	private suspend fun requestPermission(permission: String): Boolean {
		if (isGranted(permission)) return true
		return requestMutex.withLock {
			isGranted(permission) || launchPermissionRequest(permission)
		}
	}

	private suspend fun launchPermissionRequest(permission: String): Boolean {
		return withContext(Dispatchers.Main.immediate) {
			val activity = activities.get<ComponentActivity>()

			suspendCancellableCoroutine { continuation ->
				lateinit var launcher: ActivityResultLauncher<String>
				lateinit var observer: LifecycleEventObserver

				fun cleanup() {
					launcher.unregister()
					activity.lifecycle.removeObserver(observer)
				}

				launcher = activity.activityResultRegistry.register(
					UUID.randomUUID().toString(),
					activity,
					ActivityResultContracts.RequestPermission()
				) { isGranted ->
					cleanup()
					if (continuation.isActive) continuation.resume(isGranted)
				}

				observer = LifecycleEventObserver { _, event ->
					if (event == Lifecycle.Event.ON_DESTROY) {
						cleanup()
						if (continuation.isActive) continuation.resume(false)
					}
				}
				activity.lifecycle.addObserver(observer)

				continuation.invokeOnCancellation { cleanup() }

				launcher.launch(permission)
			}
		}
	}
}
