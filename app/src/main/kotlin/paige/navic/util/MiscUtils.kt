package paige.navic.util

import android.content.Context
import android.content.Intent


object MiscUtils {
	// https://stackoverflow.com/a/46848226
	fun triggerRebirth(context: Context) {
		val packageManager = context.packageManager
		val intent = packageManager.getLaunchIntentForPackage(context.packageName)
		val componentName = intent!!.component
		val mainIntent = Intent.makeRestartActivityTask(componentName)
		// Required for API 34 and later
		// Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
		mainIntent.setPackage(context.packageName)
		context.startActivity(mainIntent)
		Runtime.getRuntime().exit(0)
	}
}
