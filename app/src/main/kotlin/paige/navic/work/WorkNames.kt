/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.work

object WorkNames {
	const val DOWNLOAD_QUEUE = "download_queue"
	const val DOWNLOAD_MIGRATION = "download_migration"
	const val LIBRARY_SYNC_PERIODIC = "library_sync_periodic"
	const val LIBRARY_SYNC_MANUAL = "library_sync_manual"
	const val SYNC_ACTIONS = "sync_actions"

	const val DOWNLOAD_TAG = "download"
	const val SYNC_TAG = "sync"
	const val SYNC_ACTION_TAG = "sync_action"
}

object WorkKeys {
	const val SONG_ID = "song_id"
	const val FORCE = "force"
	const val PERIODIC = "periodic"
	const val STORAGE = "storage"

	const val PROGRESS = "progress"
	const val MESSAGE = "message"
	const val COMPLETED = "completed"
	const val TOTAL = "total"
}
