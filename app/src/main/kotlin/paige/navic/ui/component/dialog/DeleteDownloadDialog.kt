/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete

sealed interface DeleteDownloadTarget {
	data class Single(val id: String, val title: String) : DeleteDownloadTarget
	data class Collection(val name: String, val songIds: List<String>) : DeleteDownloadTarget
	data object Library : DeleteDownloadTarget
}

@Composable
fun DeleteDownloadDialog(
	target: DeleteDownloadTarget?,
	onDismissRequest: () -> Unit,
	onConfirmDelete: () -> Unit,
) {
	if (target != null) {
		val message = when (target) {
			is DeleteDownloadTarget.Single -> stringResource(
				R.string.info_delete_download_single,
				target.title,
			)

			is DeleteDownloadTarget.Collection -> stringResource(
				R.string.info_delete_download_collection,
				target.name
			)

			is DeleteDownloadTarget.Library -> stringResource(
				R.string.info_delete_download_library
			)
		}

		FormDialog(
			onDismissRequest = onDismissRequest,
			icon = { Icon(Icons.Outlined.Delete, null) },
			title = { Text(stringResource(R.string.title_delete_download)) },
			content = { Text(message) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						onConfirmDelete()
						onDismissRequest()
					},
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.errorColors()
				) {
					Text(stringResource(R.string.action_delete))
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
}
