/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2022-2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.Objects

class ActivityProvider(application: Application) : Application.ActivityLifecycleCallbacks {
	private var activeActivity: Activity? = null

	/**
	 * Gets the current active activity as a specific activity or errors otherwise.
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T : Activity> get(): T = Objects.requireNonNull(activeActivity, "No active activity cached!") as T

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
	override fun onActivityPaused(activity: Activity) {}
	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
	override fun onActivityStarted(activity: Activity) {}
	override fun onActivityStopped(activity: Activity) {}

	override fun onActivityResumed(activity: Activity) {
		activeActivity = activity
	}

	override fun onActivityDestroyed(activity: Activity) {
		activeActivity = null
	}

	init {
		application.registerActivityLifecycleCallbacks(this)
	}
}
