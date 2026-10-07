/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import paige.navic.ui.core.UiState

abstract class AsyncViewModel<T>(
	initialState: UiState<T> = UiState.Loading()
) : ViewModel() {
	data class SelectionData<T>(
		val items: List<T> = emptyList(),
		val selectedItem: T? = null
	)

	private val _uiState = MutableStateFlow<UiState<T>>(initialState)
	val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

	protected fun execute(block: suspend CoroutineScope.() -> T) {
		viewModelScope.launch {
			_uiState.value = UiState.Loading()
			runCatching {
				block()
			}.onSuccess { result ->
				_uiState.value = UiState.Success(result)
			}.onFailure { error ->
				_uiState.value = UiState.Error(Exception(error))
			}
		}
	}

	protected fun launch(block: suspend CoroutineScope.() -> Unit) {
		viewModelScope.launch {
			block()
		}
	}

	protected fun updateState(transform: (UiState<T>) -> UiState<T>) {
		_uiState.update(transform)
	}
}
