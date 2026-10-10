/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import paige.navic.R
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.ui.viewmodel.AsyncViewModel

class RadioCreateDialogViewModel(
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : AsyncViewModel<Nothing>() {
	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()
	val streamUrl = TextFieldState()
	val homepageUrl = TextFieldState()

	fun create() {
		launch {
			sessionManager.api.createInternetRadioStation(
				name = name.text.toString(),
				streamUrl = streamUrl.text.toString(),
				homepageUrl = homepageUrl.text.toString().trim().takeIf { it.isNotBlank() }
			)
			_events.send(Event.Dismiss)
			snackBarManager.notify(R.string.notice_created_radio, name.text.toString())
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}
