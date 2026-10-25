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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.settings.DownloadDeletionType
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.common.SegmentedListButtonDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete

sealed interface DeleteDownloadTarget {
	data class Single(val id: String, val title: String) : DeleteDownloadTarget
	data class Collection(
		val name: String,
		val songIds: List<String>,
		val type: DownloadDeletionType
	) : DeleteDownloadTarget {
		companion object {
			fun of(collection: DomainSongCollection): Collection {
				val name = collection.name ?: when (collection) {
					is DomainAlbum -> "unknown album"
					is DomainPlaylist -> "unknown playlist"
				}
				return Collection(
					name = name,
					songIds = collection.songs.map { it.id },
					type = if (collection is DomainAlbum) DownloadDeletionType.ALBUM else DownloadDeletionType.PLAYLIST
				)
			}
		}
	}
	data object Library : DeleteDownloadTarget
}

class DownloadDeletionHandler(
	private val downloadManager: DownloadManager,
	private val preferenceManager: PreferenceManager,
	private val snackBarManager: SnackBarManager
) {
	var activeTarget by mutableStateOf<DeleteDownloadTarget?>(null)
	private var pendingCallback: (() -> Unit)? = null

	fun requestDelete(
		target: DeleteDownloadTarget,
		notifySnackBar: Boolean = false,
		onConfirmed: (() -> Unit)? = null
	) {
		when (target) {
			is DeleteDownloadTarget.Single -> {
				if (preferenceManager.isConfirmDeleteEnabled(DownloadDeletionType.SONG)) {
					activeTarget = target
					pendingCallback = onConfirmed
				} else {
					downloadManager.deleteDownload(target.id)
					if (notifySnackBar) snackBarManager.notify(R.string.notice_deleted_download)
					onConfirmed?.invoke()
				}
			}

			is DeleteDownloadTarget.Collection -> {
				if (target.songIds.isEmpty() && onConfirmed == null) return
				if (preferenceManager.isConfirmDeleteEnabled(target.type)) {
					activeTarget = target
					pendingCallback = onConfirmed
				} else {
					if (target.songIds.isNotEmpty()) {
						downloadManager.deleteDownloadedCollection(target.songIds)
					}
					if (notifySnackBar) snackBarManager.notify(R.string.notice_deleted_download)
					onConfirmed?.invoke()
				}
			}

			is DeleteDownloadTarget.Library -> {
				if (preferenceManager.isConfirmDeleteEnabled(DownloadDeletionType.LIBRARY)) {
					activeTarget = target
					pendingCallback = onConfirmed
				} else {
					downloadManager.clearAllDownloads()
					if (notifySnackBar) snackBarManager.notify(R.string.notice_deleted_download)
					onConfirmed?.invoke()
				}
			}
		}
	}

	fun confirmDelete() {
		val target = activeTarget ?: return
		activeTarget = null

		when (target) {
			is DeleteDownloadTarget.Single -> {
				downloadManager.deleteDownload(target.id)
			}

			is DeleteDownloadTarget.Collection -> {
				if (target.songIds.isNotEmpty()) {
					downloadManager.deleteDownloadedCollection(target.songIds)
				}
			}

			is DeleteDownloadTarget.Library -> {
				downloadManager.clearAllDownloads()
			}
		}
		snackBarManager.notify(R.string.notice_deleted_download)
		pendingCallback?.invoke()
		pendingCallback = null
	}

	fun dismiss() {
		activeTarget = null
		pendingCallback = null
	}
}

@Composable
fun rememberDownloadDeletionHandler(
	downloadManager: DownloadManager = koinInject(),
	preferenceManager: PreferenceManager = koinInject(),
	snackBarManager: SnackBarManager = koinInject()
): DownloadDeletionHandler {
	val handler = remember(downloadManager, preferenceManager, snackBarManager) {
		DownloadDeletionHandler(downloadManager, preferenceManager, snackBarManager)
	}

	DeleteDownloadDialog(
		target = handler.activeTarget,
		onDismissRequest = handler::dismiss,
		onConfirmDelete = { handler.confirmDelete() }
	)

	return handler
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
