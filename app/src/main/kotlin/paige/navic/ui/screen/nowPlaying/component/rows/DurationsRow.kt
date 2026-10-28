/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.component.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.playback.MediaPlayer
import paige.navic.util.toHoursMinutesSeconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun NowPlayingDurationsRow() {
	val preferenceManager = koinInject<PreferenceManager>()
	val player = koinInject<MediaPlayer>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val duration = playerState.currentSong?.duration
	val style = MaterialTheme.typography.bodyMedium
		.copy(
			shadow = Shadow(
				color = MaterialTheme.colorScheme.inverseOnSurface,
				offset = Offset(0f, 4f),
				blurRadius = 10f
			)
		)
	val color = MaterialTheme.colorScheme.onSurfaceVariant
	Row(Modifier.padding(horizontal = 16.dp)) {
		when {
			duration == kotlin.time.Duration.ZERO -> {
				Text(text = "LIVE", color = color, style = style)
				Spacer(Modifier.weight(1f))
				Text(text = "∞", color = color, style = style)
			}

			duration != null -> {
				val currentSeconds = (duration.inWholeSeconds * playerState.progress).toLong()
				val remainingSeconds = (duration.inWholeSeconds - currentSeconds).coerceAtLeast(0)
				val elapsedText = if (preferenceManager.nowPlayingShowTimeRemaining) {
					"-${remainingSeconds.seconds.toHoursMinutesSeconds()}"
				} else {
					currentSeconds.seconds.toHoursMinutesSeconds()
				}

				Text(
					text = elapsedText,
					color = color,
					style = style,
					modifier = Modifier.clickable(
						interactionSource = remember { MutableInteractionSource() }
					) {
						preferenceManager.nowPlayingShowTimeRemaining =
							!preferenceManager.nowPlayingShowTimeRemaining
					}
				)
				Spacer(Modifier.weight(1f))
				Text(
					text = duration.toHoursMinutesSeconds(),
					color = color,
					style = style,
					modifier = Modifier.clickable(
						interactionSource = remember { MutableInteractionSource() }
					) {
						preferenceManager.nowPlayingShowTimeRemaining =
							!preferenceManager.nowPlayingShowTimeRemaining
					}
				)
			}

			else -> {
				Text("--:--", color = color, style = style)
				Spacer(Modifier.weight(1f))
				Text("--:--", color = color, style = style)
			}
		}
	}
}
