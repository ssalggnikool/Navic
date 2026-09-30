/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.radio.component

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.domain.model.DomainRadio
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.core.UiState

fun LazyGridScope.radioListScreenContent(
	state: UiState<List<DomainRadio>>,
	onRadioClick: (DomainRadio) -> Unit
) {
	val data = state.data.orEmpty()

	if (data.isNotEmpty()) {
		items(data, key = { it.id }) { radio ->
			RadioListScreenCard(
				modifier = Modifier.animateItem(),
				radio = radio,
				onPlayClick = { onRadioClick(radio) }
			)
		}
	} else {
		when (state) {
			is UiState.Loading -> {
				items(10) {
					RadioListScreenCardPlaceholder()
				}
			}

			else -> {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						label = stringResource(R.string.info_no_radios)
					)
				}
			}
		}
	}
}
