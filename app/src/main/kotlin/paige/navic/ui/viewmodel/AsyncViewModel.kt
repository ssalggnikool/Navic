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
	initialState: UiState<T> = UiState.Loading(),
	initialValue: T? = null
) : ViewModel() {
	private val _uiState = MutableStateFlow<UiState<T>>(initialState)
	val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()
	val data
		get() = uiState.value.data

	init {
	    if (initialValue != null) {
			_uiState.value = UiState.Loading(initialValue)
		}
	}

	protected fun execute(block: suspend CoroutineScope.(T?) -> T) {
		viewModelScope.launch {
			setLoading()

			runCatching {
				block.invoke(this, _uiState.value.data)
			}.onSuccess { result ->
				_uiState.value = UiState.Success(result)
			}.onFailure { error ->
				updateState { UiState.Error(Exception(error), it.data) }
			}
		}
	}

	protected fun launch(block: suspend CoroutineScope.() -> Unit) {
		viewModelScope.launch {
			runCatching {
				block()
			}.onFailure { error ->
				updateState { UiState.Error(Exception(error), it.data) }
			}
		}
	}

	protected fun updateState(transform: (UiState<T>) -> UiState<T>) {
		_uiState.update(transform)
	}

	protected fun setLoading() {
		updateState { UiState.Loading(it.data) }
	}

	protected fun setSuccess(data: T) {
		_uiState.value = UiState.Success(data)
	}

	protected fun updateData(transform: (T) -> T) {
		updateState { state ->
			state.data?.let { UiState.Success(transform(it)) } ?: state
		}
	}

	fun clearError() {
		updateState { state ->
			if (state is UiState.Error) {
				state.data?.let { UiState.Success(it) } ?: UiState.Loading()
			} else {
				state
			}
		}
	}
}
