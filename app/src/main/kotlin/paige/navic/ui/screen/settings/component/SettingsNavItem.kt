/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.component

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.ChevronForward

@Composable
fun SettingsNavItem(
	onClick: () -> Unit,
	enabled: Boolean = true,
	shapes: ListItemShapes = SegmentedListItemDefaults.segmentedShapes(),
	leadingContent: @Composable (() -> Unit)? = null,
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	SegmentedListItem(
		onClick = onClick,
		enabled = enabled,
		shapes = shapes,
		supportingContent = supportingContent,
		leadingContent = leadingContent,
		trailingContent = { Icon(Icons.Outlined.ChevronForward, null) },
		content = content
	)
}
