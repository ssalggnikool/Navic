/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import paige.navic.ui.component.common.SegmentedListItem

@Composable
fun SettingsRadioItem(
	selected: Boolean,
	onClick: () -> Unit,
	enabled: Boolean = true,
	shapes: ListItemShapes,
	contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	SegmentedListItem(
		onClick = onClick,
		enabled = enabled,
		selected = selected,
		shapes = shapes,
		contentPadding = contentPadding,
		content = content,
		supportingContent = supportingContent,
		leadingContent = {
			RadioButton(
				selected = selected,
				onClick = null
			)
		}
	)
}
