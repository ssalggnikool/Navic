/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import paige.navic.R
import paige.navic.domain.model.settings.AppIconVariant

class AppIconManager(
	private val context: Context,
	private val preferenceManager: PreferenceManager
) {
	fun setVariant(newVariant: AppIconVariant) {
		Toast.makeText(
			context,
			"App icon changed, closing app!!",
			Toast.LENGTH_SHORT
		).show()
		preferenceManager.appIconVariant = newVariant
		AppIconVariant.entries.forEach { variant ->
			context.packageManager.setComponentEnabledSetting(
				ComponentName(
					context.packageName,
					"${context.packageName}.${variant.activityName}"
				),
				if (variant == newVariant) {
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				} else {
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				},
				PackageManager.DONT_KILL_APP
			)
		}
	}

	fun getIcon(variant: AppIconVariant): Any? {
		val resources = context.resources

		val iconRes = when (variant) {
			AppIconVariant.Default -> R.mipmap.ic_launcher
			AppIconVariant.Inverted -> R.mipmap.ic_launcher_inverted
		}

		val drawable = if (iconRes != 0) {
			ResourcesCompat.getDrawable(resources, iconRes, null)
		} else {
			val activityName = "${context.packageName}.${variant.activityName}"
			val componentName = ComponentName(context.packageName, activityName)
			try {
				val info = context.packageManager.getActivityInfo(
					componentName,
					PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
				)
				val activityResources = context.packageManager.getResourcesForActivity(componentName)
				ResourcesCompat.getDrawable(activityResources, info.icon, null)
			} catch (_: Exception) {
				null
			}
		} ?: context.packageManager.getApplicationIcon(context.packageName)

		return drawable.toBitmap(512, 512).asImageBitmap()
	}
}
