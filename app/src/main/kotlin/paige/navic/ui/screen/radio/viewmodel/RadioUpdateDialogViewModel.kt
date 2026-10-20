/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.model.DomainRadio
import paige.navic.ui.core.UiState

class RadioUpdateDialogViewModel(
	private val initialRadio: DomainRadio,
	private val sessionManager: SessionManager
) : ViewModel() {
	val updateState: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState(initialRadio.name)
	val streamUrl = TextFieldState(initialRadio.streamUrl)
	val homepageUrl = TextFieldState(initialRadio.homepageUrl.orEmpty())

	fun update() {
		viewModelScope.launch {
			updateState.value = UiState.Loading()
			try {
				sessionManager.api.updateInternetRadioStation(
					id = initialRadio.id,
					streamUrl = streamUrl.text.toString(),
					name = name.text.toString(),
					homepageUrl = homepageUrl.text.toString().takeIf { it.isNotBlank() }
				)
				_events.send(Event.Dismiss)
				updateState.value = UiState.Success(null)
			} catch (ex: Exception) {
				updateState.value = UiState.Error(ex)
			}
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}
