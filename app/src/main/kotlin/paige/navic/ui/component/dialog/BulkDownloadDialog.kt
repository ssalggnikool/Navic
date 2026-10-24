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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Download

sealed interface BulkDownloadTarget {
	data class Artist(
		val name: String,
		val songIds: List<String>
	) : BulkDownloadTarget

	data object Library : BulkDownloadTarget
}

@Composable
fun BulkDownloadDialog(
	target: BulkDownloadTarget?,
	showDialog: Boolean = target != null,
	onDismissRequest: () -> Unit
) {
	val downloadManager = koinInject<DownloadManager>()
	val snackBarManager = koinInject<SnackBarManager>()
	val scope = rememberCoroutineScope()

	if (showDialog && target != null) {
		val (title, message) = when (target) {
			is BulkDownloadTarget.Artist -> {
				stringResource(R.string.title_bulk_download) to stringResource(
					R.string.info_bulk_download_warning,
					target.name
				)
			}

			is BulkDownloadTarget.Library -> {
				stringResource(R.string.title_library_download) to stringResource(
					R.string.info_library_download_warning
				)
			}
		}

		FormDialog(
			onDismissRequest = onDismissRequest,
			icon = { Icon(Icons.Outlined.Download, null) },
			title = { Text(title) },
			content = { Text(message) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						scope.launch {
							when (target) {
								is BulkDownloadTarget.Artist -> {
									downloadManager.downloadCollection(target.songIds)
								}

								is BulkDownloadTarget.Library -> {
									downloadManager.downloadEntireLibrary()
								}
							}
							snackBarManager.notify(R.string.notice_download_started)
						}
						onDismissRequest()
					},
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.primaryColors()
				) {
					Text(stringResource(R.string.action_download))
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
