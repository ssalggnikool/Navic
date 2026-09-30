/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.component.controls

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreHoriz
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.sheet.SleepTimerSheet
import paige.navic.ui.component.sheet.SongSheet
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.playlist.dialog.PlaylistUpdateDialog
import paige.navic.ui.screen.share.dialog.ShareDialog
import paige.navic.ui.theme.NavicTheme
import paige.navic.ui.util.rememberColorSchemeFromCoverArt
import kotlin.time.Duration

@Composable
fun NowPlayingMoreButton(
	songRating: Int,
	onSetSongRating: (Int) -> Unit
) {
	val backStack = LocalNavStack.current
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val song = playerState.currentSong
	var expanded by remember { mutableStateOf(false) }
	var sleepTimerSheetShown by rememberSaveable { mutableStateOf(false) }
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	val colorScheme = rememberColorSchemeFromCoverArt(song?.coverArtId)

	IconButton(
		onClick = {
			expanded = true
		},
		colors = IconButtonDefaults.filledTonalIconButtonColors(),
		modifier = Modifier.size(32.dp),
		enabled = song != null
	) {
		Icon(
			imageVector = Icons.Outlined.MoreHoriz,
			contentDescription = stringResource(R.string.action_more)
		)
	}

	if (expanded && song != null) {
		NavicTheme(colorScheme) {
			SongSheet(
				onDismissRequest = { expanded = false },
				song = song,
				collection = playerState.currentCollection,
				onViewAlbum = dropUnlessResumed {
					playerState.currentCollection?.let { collection ->
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.CollectionDetail(collection.id, ""))
					}
				},
				onViewArtist = dropUnlessResumed {
					backStack.remove(Screen.NowPlaying)
					backStack.add(Screen.ArtistDetail(song.artistId))
				},
				onShare = {
					shareId = song.id
				},
				onAddToPlaylist = {
					playlistDialogShown = true
				},
				onTrackInfo = dropUnlessResumed {
					expanded = false
					backStack.add(Screen.SongDetailSheet(songId = song.id, coverArtId = song.coverArtId))
				},
				rating = songRating,
				onSetRating = onSetSongRating,
				showSleepTimer = true,
				onSleepTimer = {
					expanded = false
					sleepTimerSheetShown = true
				},
				showPlaybackSpeed = true,
				onPlaybackSpeed = {
					expanded = false
					backStack.add(Screen.PlaybackSpeed)
				}
			)
		}
	}

	if (sleepTimerSheetShown) {
		NavicTheme(colorScheme) {
			SleepTimerSheet(
				onDismissRequest = { sleepTimerSheetShown = false }
			)
		}
	}

	if (playlistDialogShown && song != null) {
		NavicTheme(colorScheme) {
			PlaylistUpdateDialog(
				songs = persistentListOf(song),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}

	NavicTheme(colorScheme) {
		ShareDialog(
			id = shareId,
			onIdClear = { shareId = null },
			expiry = shareExpiry,
			onExpiryChange = { shareExpiry = it }
		)
	}
}
