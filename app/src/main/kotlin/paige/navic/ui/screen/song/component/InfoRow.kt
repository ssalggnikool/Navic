/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.song.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.ui.component.common.SegmentedListItem

@Composable
fun SongDetailScreenInfoRow(
	shapes: ListItemShapes,
	key: String,
	value: String?
) {
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	SegmentedListItem(
		shapes = shapes,
		contentPadding = PaddingValues(14.dp),
		overlineContent = {
			Text(
				text = key,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary
			)
		},
		content = {
			Text(
				text = value ?: stringResource(R.string.info_unknown),
				style = MaterialTheme.typography.bodyLarge
			)
		},
		onClick = {
			if (value == null) return@SegmentedListItem
			clipboard.setText(AnnotatedString(value))
		}
	)
}
