/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.viewmodel


data class Selectable<T>(
	val items: List<T>,
	val selected: T?
)

abstract class SelectableViewModel<T> : AsyncViewModel<Selectable<T>>() {
	val data
		get() = uiState.value.data

	val items
		get() = data?.items

	val selected
		get() = data?.selected

	fun select(item: T?) {
		execute {
			Selectable(
				items = uiState.value.data?.items ?: emptyList(),
				selected = item
			)
		}
	}

	fun clear() = select(null)
}
