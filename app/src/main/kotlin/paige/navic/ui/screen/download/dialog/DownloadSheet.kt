/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.download.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.ui.component.sheet.ModalBottomSheet
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Close
import paige.navic.ui.icons.outlined.Delete

@Composable
fun DownloadSheet(
	onDismissRequest: () -> Unit,
	download: DownloadEntity,
	onDelete: () -> Unit
) {
	val contentPadding = PaddingValues(horizontal = 16.dp)
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)

	@OptIn(ExperimentalMaterial3Api::class)
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = null,
		containerColor = MaterialTheme.colorScheme.surface,
		sheetState = rememberBottomSheetState(
			initialValue = SheetValue.Hidden,
			enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
		),
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 8.dp,
					right = 8.dp
				)
			)
		}
	) {
		Spacer(Modifier.height(16.dp))
		when (download.status) {
			DownloadStatus.DOWNLOADING -> {
				ListItem(
					content = { Text(stringResource(R.string.action_cancel_download)) },
					leadingContent = { Icon(Icons.Outlined.Close, null) },
					onClick = {
						onDelete()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}

			else -> {
				ListItem(
					content = { Text(stringResource(R.string.action_delete_download)) },
					leadingContent = { Icon(Icons.Outlined.Delete, null) },
					onClick = {
						onDelete()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}
		}
	}
}
