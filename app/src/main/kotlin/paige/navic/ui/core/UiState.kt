/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.core

sealed class UiState<out T> {
	abstract val data: T?

	data class Loading<T>(override val data: T? = null) : UiState<T>()
	data class Success<T>(override val data: T) : UiState<T>()
	data class Error<T>(
		val error: Exception,
		override val data: T? = null,
	) : UiState<T>()
}
