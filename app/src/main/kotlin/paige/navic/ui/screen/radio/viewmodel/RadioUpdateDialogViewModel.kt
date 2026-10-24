/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.model.DomainRadio
import paige.navic.ui.viewmodel.AsyncViewModel

class RadioUpdateDialogViewModel(
	private val initialRadio: DomainRadio,
	private val sessionManager: SessionManager
) : AsyncViewModel<Nothing>() {
	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState(initialRadio.name)
	val streamUrl = TextFieldState(initialRadio.streamUrl)
	val homepageUrl = TextFieldState(initialRadio.homepageUrl.orEmpty())

	fun update() {
		launch {
			sessionManager.api.updateInternetRadioStation(
				id = initialRadio.id,
				streamUrl = streamUrl.text.toString(),
				name = name.text.toString(),
				homepageUrl = homepageUrl.text.toString().takeIf { it.isNotBlank() }
			)
			_events.send(Event.Dismiss)
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}
