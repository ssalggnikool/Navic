/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.domain.model.DomainSong
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.filled.Note
import paige.navic.ui.icons.outlined.Radio
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.common.CoverArt

@Composable
fun NowPlayingArtwork(
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)?,
	isLandscape: Boolean,
	song: DomainSong
) {
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()

	val isRadio = song.id.startsWith("radio_")

	val padding by animateDpAsState(
		targetValue = if (playerState.isPaused || playerState.currentSong?.id != song.id)
			48.dp
		else 16.dp
	)
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		CoverArt(
			coverArtId = song.coverArtId,
			modifier = Modifier
				.then(if (isLandscape) Modifier.fillMaxHeight() else Modifier.fillMaxSize())
				.padding(padding),
			shadowElevation = 8.dp,
			onClick = onClick
		)
		if (song.coverArtId.isNullOrEmpty()) {
			Icon(
				imageVector = if (isRadio) Icons.Outlined.Radio else Icons.Filled.Note,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f),
				modifier = Modifier.size(96.dp)
			)
		}
	}
}
