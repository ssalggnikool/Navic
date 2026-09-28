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
