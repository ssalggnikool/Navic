/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.LinkManager
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults

@Composable
fun LinkConfirmationDialog(
	linkToOpen: String,
	onDismissRequest: () -> Unit
) {
	val linkManager = koinInject<LinkManager>()

	FormDialog(
		onDismissRequest = onDismissRequest,
		title = { Text(stringResource(R.string.title_link_confirmation)) },
		content = {
			Text(stringResource(R.string.notice_link_confirmation))
			Spacer(Modifier.height(8.dp))
			Surface(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.large,
				color = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp),
					horizontalArrangement = Arrangement.Center
				) {
					Text(
						text = linkToOpen,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		},
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					linkManager.openLink(linkToOpen)
					onDismissRequest()
				},
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2)
			) {
				Text(stringResource(R.string.action_visit_site))
			}
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = onDismissRequest,
				shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
			) {
				Text(stringResource(R.string.action_cancel))
			}
		}
	)
}
