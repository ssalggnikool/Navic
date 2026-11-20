/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import paige.navic.MainActivity
import paige.navic.playback.exoplayer.ExoPlaybackService
import kotlin.system.exitProcess

internal fun preferencesFile(context: Context) = "${context.packageName}_preferences"

class AppRestarter(
	private val context: Context
) {
	@SuppressLint("UseKtx", "ApplySharedPref")
	fun restart() {
		// forcefully commit all the edit writes to disk
		context.getSharedPreferences(
			preferencesFile(context),
			Context.MODE_PRIVATE
		).edit().commit()

		runCatching {
			context.stopService(Intent(context, ExoPlaybackService::class.java))
		}

		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		context.startActivity(intent)
		exitProcess(0)
	}
}
