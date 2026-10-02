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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.filled.Star
import paige.navic.ui.icons.outlined.Star
import paige.navic.shared.MediaPlayerViewModel

@Composable
fun NowPlayingStarButton(
	songIsStarred: Boolean,
	onSetSongIsStarred: (Boolean) -> Unit
) {
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	IconButton(
		onClick = {
			onSetSongIsStarred(!songIsStarred)
		},
		colors = IconButtonDefaults.filledTonalIconButtonColors(),
		modifier = Modifier.size(32.dp),
		enabled = playerState.currentSong != null
	) {
		Icon(
			if (songIsStarred) Icons.Filled.Star else Icons.Outlined.Star,
			contentDescription = stringResource(R.string.action_star)
		)
	}
}
