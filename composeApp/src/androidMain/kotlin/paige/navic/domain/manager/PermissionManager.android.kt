package paige.navic.domain.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import paige.navic.di.ActivityProvider
import java.util.UUID
import kotlin.coroutines.resume

actual class PermissionManager(
	private val context: Context,
	private val activities: ActivityProvider
) {
	private var pendingContinuation: CancellableContinuation<Boolean>? = null
	private var permissionLauncher = run {
		val activity = activities.get<ComponentActivity>()
		activity.activityResultRegistry.register(
			key = UUID.randomUUID().toString(),
			contract = ActivityResultContracts.RequestPermission(),
			callback = { isGranted ->
				pendingContinuation?.resume(isGranted)
				pendingContinuation = null
			},
		)
	}

	actual fun openPermissionsSettings() {
		val activity = activities.get<ComponentActivity>()
		val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
		intent.data = Uri.fromParts("package", activity.packageName, null)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
		activity.startActivity(intent)
	}

	actual suspend fun requestLocalNetworkPermission(): Boolean {
		if (Build.VERSION.SDK_INT < 37) {
			// this permission is only needed on android 17
			return true
		}

		val alreadyGranted = context.checkSelfPermission(
			Manifest.permission.ACCESS_LOCAL_NETWORK
		) == PackageManager.PERMISSION_GRANTED

		if (alreadyGranted) return true

		return suspendCancellableCoroutine { continuation ->
			pendingContinuation = continuation
			continuation.invokeOnCancellation {
				pendingContinuation = null
			}
			permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
		}
	}
}
