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
	var items
		get() = data?.items
		set(value) {
			execute {
				Selectable(
					items = value ?: emptyList(),
					selected = selected
				)
			}
		}

	var selected
		get() = data?.selected
		set(value) {
			execute {
				Selectable(
					items = items ?: emptyList(),
					selected = value
				)
			}
		}

	fun clear() {
		selected = null
	}
}
