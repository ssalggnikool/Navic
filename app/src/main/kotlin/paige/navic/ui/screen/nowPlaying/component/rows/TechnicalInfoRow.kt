/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.component.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.playback.MediaPlayer
import paige.navic.playback.PlaybackUtils

@Composable
fun NowPlayingTechnicalInfoRow() {
	val player = koinInject<MediaPlayer>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val song = playerState.currentSong

	val style = MaterialTheme.typography.bodySmall
	val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.Center
	) {
		Box(contentAlignment = Alignment.Center) {

			Box(
				modifier = Modifier
					.matchParentSize()
					.clip(CircleShape)
					.blur(8.dp)
					.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
			)

			Row(
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				val sampleRateFormatted = (playerState.playbackSampleRate)?.let {
						if (it >= 1000) "${it / 1000.0} kHz" else "$it Hz"
					} ?: "-- kHz"

				val bitDepthFormatted = (playerState.playbackBitDepth)
					?.takeIf { it > 16 }
					?.let { "$it bit" }

				val bitrateFormatted =
					(playerState.playbackBitrate?.let { it / 1000 })?.let { "$it kbps" }
						?: "-- kbps"

				val format =
					playerState.playbackMimeType
						?.let { PlaybackUtils.getPrettyFormatName(it) }
						?.uppercase()
						?: song?.fileExtension?.uppercase()
						?: "--"

				Text(
					text = bitDepthFormatted?.let { "$format • $it / $sampleRateFormatted • $bitrateFormatted" }
						?: "$format • $sampleRateFormatted • $bitrateFormatted",
					style = style,
					color = color
				)
			}
		}
	}
}
