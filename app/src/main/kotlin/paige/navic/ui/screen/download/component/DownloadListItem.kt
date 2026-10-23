/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.download.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import paige.navic.database.entity.DownloadEntity
import paige.navic.ui.component.common.SwipeToDismissBox
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete

@Composable
fun DownloadListItem(
	modifier: Modifier = Modifier,
	download: DownloadEntity,
	onClick: () -> Unit,
	onDelete: () -> Unit
) {
	val scope = rememberCoroutineScope()
	val dismissState = rememberSwipeToDismissBoxState()

	SwipeToDismissBox(
		modifier = modifier,
		state = dismissState,
		onDismiss = {
			if (it != SwipeToDismissBoxValue.Settled) onDelete()
			scope.launch { dismissState.reset() }
		},
		backgroundContent = {
			if (dismissState.dismissDirection == SwipeToDismissBoxValue.Settled) return@SwipeToDismissBox
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(MaterialTheme.shapes.small)
					.background(MaterialTheme.colorScheme.errorContainer)
					.padding(horizontal = 20.dp)
			) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.align(
						when (dismissState.dismissDirection) {
							SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
							else -> Alignment.CenterEnd
						}
					)
				)
			}
		}
	) {
		ListItem(
			content = { Text(download.songId) },
			onClick = onClick
		)
	}
}
