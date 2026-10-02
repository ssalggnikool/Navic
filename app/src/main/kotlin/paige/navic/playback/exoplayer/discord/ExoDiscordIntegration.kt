/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer.discord

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import night.milkyway.antisocialcord.DiscordRpcClient
import night.milkyway.antisocialcord.model.Activity
import night.milkyway.antisocialcord.model.ActivityAssets
import night.milkyway.antisocialcord.model.ActivityTimestamps
import night.milkyway.antisocialcord.model.ActivityType
import night.milkyway.antisocialcord.model.StatusDisplayType
import paige.navic.domain.manager.PreferenceManager

class ExoDiscordIntegration(
	context: Context,
	val player: Player,
	val preferenceManager: PreferenceManager
) : Player.Listener {
	companion object {
		const val DISCORD_APPLICATION_ID = 1554936646405984446L
		const val INVISIBLE_CHARACTER = '\u00A0'
		const val TAG = "ExoDiscordIntegration"
	}

	private val client = DiscordRpcClient(context)
	private val scope = CoroutineScope(Dispatchers.Main)
	private val mutex = Mutex()

	fun isIntegrationEnabled(): Boolean {
		return preferenceManager.discordIntegrationEnabled
	}

	override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
		if (playbackState == Player.STATE_READY) {
			setCurrentActivity(player.currentMediaItem)
		}
	}

	override fun onPositionDiscontinuity(
		oldPosition: Player.PositionInfo,
		newPosition: Player.PositionInfo,
		reason: @Player.DiscontinuityReason Int
	) {
		val isSeekOrRepeat = reason == Player.DISCONTINUITY_REASON_SEEK ||
			reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION

		val jumpedToStart = newPosition.positionMs < 5000L
		val wasFurtherAlong = oldPosition.positionMs > 10000L

		if (isSeekOrRepeat && jumpedToStart && wasFurtherAlong) {
			if (player.currentMediaItem != null) {
				setCurrentActivity(player.currentMediaItem)
			}
		}
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		if (!isPlaying) {
			setCurrentActivity(null)
		} else {
			setCurrentActivity(player.currentMediaItem)
		}
	}

	private fun setCurrentActivity(mediaItem: MediaItem?) {
		scope.launch {
			mutex.withLock {
				try {
					if (isIntegrationEnabled()) {
						if (!client.isConnectionReady) {
							client.connect(DISCORD_APPLICATION_ID)
						}

						if (mediaItem != null) {
							val activity = createDiscordActivity(mediaItem)
							client.setActivity(activity)
							return@withLock
						}
					} else {
						if (client.isConnectionReady) {
							client.disconnect()
						}
					}

					client.clearActivity()
				} catch (e: Exception) {
					Log.d(TAG, "fuck this discord shit", e)
				}
			}
		}
	}

	private fun createDiscordActivity(mediaItem: MediaItem): Activity {
		val metadata = mediaItem.mediaMetadata

		val nowMs = System.currentTimeMillis()
		val currentPosMs = player.currentPosition.coerceAtLeast(0L)
		val startSec = (nowMs - currentPosMs)
		val endSec = if (player.duration > 0) {
			(startSec + player.duration)
		} else {
			null
		}

		val artworkUrl = metadata.artworkUri?.toString()?.let {
			if (it.length > 256) {
				null
			} else {
				it
			}
		}

		return Activity(
			name = "Navic",
			activityType = ActivityType.LISTENING,
			state = metadata.artist?.toString()?.padEnd(2, INVISIBLE_CHARACTER),
			details = metadata.title?.toString()?.padEnd(2, INVISIBLE_CHARACTER),
			statusDisplayType = StatusDisplayType.STATE,
			assets = ActivityAssets(
				largeUrl = null,
				largeImage = artworkUrl,
				largeText = metadata.albumTitle?.toString()?.padEnd(2, INVISIBLE_CHARACTER),
				smallImage = null,
				smallText = null,
				smallUrl = null
			),
			timestamps = if (endSec != null) {
				ActivityTimestamps(startSec, endSec)
			} else {
				null
			}
		)
	}
}
