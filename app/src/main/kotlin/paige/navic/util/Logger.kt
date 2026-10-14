/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import android.util.Log

object Logger {
	fun e(tag: String, msg: String, tr: Throwable? = null) {
		Log.e(tag, msg, tr)
	}

	fun i(tag: String, msg: String, tr: Throwable? = null) {
		Log.i(tag, msg, tr)
	}

	fun w(tag: String, msg: String, tr: Throwable? = null) {
		Log.w(tag, msg, tr)
	}
}
