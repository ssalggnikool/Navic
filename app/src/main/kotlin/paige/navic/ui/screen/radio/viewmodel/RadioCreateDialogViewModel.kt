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
import paige.navic.R
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.ui.core.UiState

class RadioCreateDialogViewModel(
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	val creationState: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()
	val streamUrl = TextFieldState()
	val homepageUrl = TextFieldState()

	fun create() {
		viewModelScope.launch {
			creationState.value = UiState.Loading()
			try {
				sessionManager.api.createInternetRadioStation(
					name = name.text.toString(),
					streamUrl = streamUrl.text.toString(),
					homepageUrl = homepageUrl.text.toString().trim().takeIf { it.isNotBlank() }
				)
				_events.send(Event.Dismiss)
				creationState.value = UiState.Success(null)
				snackBarManager.notify(R.string.notice_created_radio, name.text.toString())
			} catch (e: Exception) {
				creationState.value = UiState.Error(e)
			}
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}
