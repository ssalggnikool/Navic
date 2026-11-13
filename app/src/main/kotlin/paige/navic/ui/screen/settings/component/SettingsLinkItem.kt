/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.component

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.dialog.LinkConfirmationDialog
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.External

@Composable
fun SettingsLinkItem(
	link: String,
	shapes: ListItemShapes = SegmentedListItemDefaults.segmentedShapes(),
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	SegmentedListItem(
		onClick = { linkToOpen = link },
		shapes = shapes,
		content = content,
		supportingContent = supportingContent,
		trailingContent = {
			Icon(
				imageVector = Icons.Outlined.External,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	)

	LinkConfirmationDialog(
		linkToOpen = linkToOpen,
		onDismissRequest = { linkToOpen = null }
	)
}
