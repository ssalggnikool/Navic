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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.data.database.entity.DownloadStatus
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.canUserShare
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumInfo
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Queue
import paige.navic.icons.outlined.QueuePlayNext
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.component.common.MarqueeText
import paige.navic.ui.component.common.RatingRow
import paige.navic.ui.component.dialog.LinkConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSheet(
	onDismissRequest: () -> Unit,
	collection: DomainSongCollection?,
	albumInfo: DomainAlbumInfo? = null,
	onDownloadAll: (() -> Unit)? = null,
	onCancelDownloadAll: (() -> Unit)? = null,
	onDeleteDownloadAll: (() -> Unit)? = null,
	downloadStatus: DownloadStatus? = null,
	onShare: (() -> Unit)? = null,
	onPlayNext: (() -> Unit)? = null,
	onAddToQueue: (() -> Unit)? = null,
	onAddAllToPlaylist: (() -> Unit)? = null,
	onViewArtist: (() -> Unit)? = null,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
	onDelete: (() -> Unit)? = null,
	rating: Int? = null,
	onSetRating: ((Int) -> Unit)? = null
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val sessionManager = koinInject<SessionManager>()

	val contentPadding = PaddingValues(horizontal = 16.dp)
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)
	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = null,
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

		ListItem(
			leadingContent = {
				CoverArt(
					coverArtId = collection?.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = preferenceManager.coverArtShape.decreasedShape
				)
			},
			content = { MarqueeText(collection?.name.orEmpty()) },
			supportingContent = {
				MarqueeText(
					listOfNotNull(
						(collection as? DomainAlbum)?.artistName,
						(collection as? DomainPlaylist)?.comment,
						(collection as? DomainAlbum)?.genre,
						(collection as? DomainAlbum)?.year,
						collection?.songCount?.let {
							pluralStringResource(R.plurals.count_songs, it, it)
						}
					).joinToString(" • ")
				)
			},
			colors = colors
		)
		if (rating != null && onSetRating != null && preferenceManager.enableRatings) {
			RatingRow(
				rating = rating,
				setRating = onSetRating
			)
			Spacer(Modifier.height(14.dp))
		}

		HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

		Column(Modifier.verticalScroll(rememberScrollState())) {
			if (albumInfo?.lastFmUrl != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_view_on_lastfm)) },
					leadingContent = { Icon(Icons.Brand.Lastfm, null) },
					onClick = {
						linkToOpen = albumInfo.lastFmUrl
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}

			if (albumInfo?.musicBrainzId != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_view_on_musicbrainz)) },
					leadingContent = { Icon(Icons.Brand.Musicbrainz, null) },
					onClick = {
						linkToOpen = "https://musicbrainz.org/release/${albumInfo.musicBrainzId}"
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}

			if (onShare != null && sessionManager.canUserShare()) {
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
			}

			if (onPlayNext != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_play_next)) },
					leadingContent = { Icon(Icons.Outlined.QueuePlayNext, null) },
					onClick = {
						onPlayNext()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding,
					enabled = !collection?.songs.isNullOrEmpty()
				)
			}

			if (onAddToQueue != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_add_to_queue)) },
					leadingContent = { Icon(Icons.Outlined.Queue, null) },
					onClick = {
						onAddToQueue()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding,
					enabled = !collection?.songs.isNullOrEmpty()
				)
			}

			if (onAddAllToPlaylist != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_add_to_playlist)) },
					leadingContent = { Icon(Icons.Outlined.PlaylistAdd, null) },
					onClick = {
						onAddAllToPlaylist()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding,
					enabled = !collection?.songs.isNullOrEmpty()
				)
			}

			if (onViewArtist != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_view_artist)) },
					leadingContent = { Icon(Icons.Outlined.Artist, null) },
					onClick = {
						onViewArtist()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}


			if (starred != null && onSetStarred != null) {
				ListItem(
					content = {
						Text(stringResource(if (starred) R.string.action_remove_star else R.string.action_star))
					},
					leadingContent = {
						Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
					},
					onClick = {
						onSetStarred(!starred)
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}

			if (downloadStatus != null) {
				when (downloadStatus) {
					DownloadStatus.DOWNLOADING -> {
						ListItem(
							content = { Text(stringResource(R.string.action_cancel_download)) },
							leadingContent = { Icon(Icons.Outlined.Close, null) },
							onClick = {
								onCancelDownloadAll?.invoke()
								onDismissRequest()
							},
							colors = colors,
							contentPadding = contentPadding
						)
					}

					DownloadStatus.DOWNLOADED -> {
						ListItem(
							content = { Text(stringResource(R.string.action_delete_download)) },
							leadingContent = { Icon(Icons.Outlined.Delete, null) },
							onClick = {
								onDeleteDownloadAll?.invoke()
								onDismissRequest()
							},
							colors = colors,
							contentPadding = contentPadding
						)
					}

					DownloadStatus.FAILED -> {
						ListItem(
							content = {
								Text(
									text = stringResource(R.string.info_download_failed),
									color = MaterialTheme.colorScheme.error
								)
							},
							supportingContent = {
								Text(
									text = stringResource(R.string.info_click_to_retry),
									color = MaterialTheme.colorScheme.error,
									style = MaterialTheme.typography.labelSmall
								)
							},
							leadingContent = {
								Icon(
									Icons.Outlined.DownloadOff,
									null,
									tint = MaterialTheme.colorScheme.error
								)
							},
							onClick = {
								onDownloadAll?.invoke()
								onDismissRequest()
							},
							colors = colors,
							contentPadding = contentPadding
						)
					}

					else -> {
						ListItem(
							content = { Text(stringResource(R.string.action_download)) },
							leadingContent = { Icon(Icons.Outlined.Download, null) },
							onClick = {
								onDownloadAll?.invoke()
								onDismissRequest()
							},
							colors = colors,
							contentPadding = contentPadding
						)
					}
				}
			} else if (onDownloadAll != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_download)) },
					leadingContent = { Icon(Icons.Outlined.Download, null) },
					onClick = {
						onDownloadAll()
						onDismissRequest()
					},
					colors = colors,
					contentPadding = contentPadding,
					enabled = !collection?.songs.isNullOrEmpty()
				)
			}

			if (onDelete != null) {
				ListItem(
					content = { Text(stringResource(R.string.action_delete)) },
					leadingContent = { Icon(Icons.Outlined.PlaylistRemove, null) },
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

	if (linkToOpen != null) {
		LinkConfirmationDialog(
			linkToOpen = linkToOpen!!,
			onDismissRequest = { linkToOpen = null }
		)
	}
}
