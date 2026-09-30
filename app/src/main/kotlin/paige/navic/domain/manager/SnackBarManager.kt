/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.domain.model.snackbars.PlayerEvent

class SnackBarManager {
	private val _events = MutableSharedFlow<PlayerEvent>()
	val events: SharedFlow<PlayerEvent> = _events.asSharedFlow()

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

	fun notify(@StringRes resource: Int, vararg args: Any) {
		scope.launch {
			_events.emit(PlayerEvent(resource, args.toList()))
		}
	}

	fun notifyAddedToQueue() = notify(R.string.notice_added_to_queue)
	fun notifyPlayNext() = notify(R.string.notice_play_next)
}
