/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.viewmodel

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow


data class Selectable<T : Any>(
	val items: Flow<PagingData<T>>?,
	val selected: T?
)

data class SelectableList<T>(
	val items: List<T>?,
	val selected: T?
)

abstract class SelectableViewModel<T : Any> : AsyncViewModel<Selectable<T>>() {
	var items
		get() = data?.items
		set(value) {
			execute {
				Selectable(
					items = value,
					selected = selected
				)
			}
		}

	var selected
		get() = data?.selected
		set(value) {
			execute {
				Selectable(
					items = items,
					selected = value
				)
			}
		}

	fun clear() {
		selected = null
	}
}
