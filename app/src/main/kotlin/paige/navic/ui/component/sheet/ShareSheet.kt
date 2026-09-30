/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Share

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
	onDismissRequest: () -> Unit,
	onShare: () -> Unit,
	onDelete: () -> Unit
) {
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)
	val contentPadding = PaddingValues(horizontal = 16.dp)

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
		Column(Modifier.verticalScroll(rememberScrollState())) {
			ListItem(
				content = { Text(stringResource(R.string.action_share)) },
				leadingContent = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					onShare()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
			ListItem(
				content = { Text(stringResource(R.string.action_delete)) },
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
