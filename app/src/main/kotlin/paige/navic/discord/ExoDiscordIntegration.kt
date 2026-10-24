/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.getSystemService
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import night.milkyway.antisocialcord.DiscordRpcClient
import night.milkyway.antisocialcord.model.Activity
import night.milkyway.antisocialcord.model.ActivityAssets
import night.milkyway.antisocialcord.model.ActivityTimestamps
import night.milkyway.antisocialcord.model.ActivityType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainSong
import paige.navic.domain.repository.AlbumRepository
import paige.navic.domain.repository.SongRepository

class ExoDiscordIntegration(
	val player: Player,
) : Player.Listener, KoinComponent {
	private val context: Context by inject()
	private val preferenceManager: PreferenceManager by inject()
	private val songRepository: SongRepository by inject()
	private val albumRepository: AlbumRepository by inject()

	private val powerManager =
		context.getSystemService<PowerManager>()!! // idk why this would ever be null but yk

	companion object {
		const val DISCORD_APPLICATION_ID = "1554936646405984446"
		const val INVISIBLE_CHARACTER = '\u00A0'
		const val TAG = "ExoDiscordIntegration"
	}

	private val client = DiscordRpcClient(context)
	private val scope = CoroutineScope(Dispatchers.Main)

	init {
		// idk how to combine flows of different types
		scope.launch {
			combine(
				snapshotFlow { preferenceManager.enableDiscordIntegration },
				snapshotFlow { preferenceManager.disableRpcWhenPowerSaving }
			) { it }.collect {
				setCurrentActivity(player.currentMediaItem)
			}
		}
		scope.launch {
			combine(
				snapshotFlow { preferenceManager.discordStatusType }
			) { it }.collect {
				setCurrentActivity(player.currentMediaItem)
			}
		}
		scope.launch {
			combine(
				snapshotFlow { preferenceManager.discordAppId }
			) { it }.collect {
				setCurrentActivity(player.currentMediaItem)
			}
		}
		context.registerReceiver(
			object : BroadcastReceiver() {
				override fun onReceive(p0: Context?, p1: Intent?) {
					scope.launch {
						setCurrentActivity(player.currentMediaItem)
					}
				}
			},
			IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
		)
	}

	override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
		scope.launch {
			if (playbackState == Player.STATE_READY) {
				setCurrentActivity(player.currentMediaItem)
			}
		}
	}

	override fun onPositionDiscontinuity(
		oldPosition: Player.PositionInfo,
		newPosition: Player.PositionInfo,
		reason: @Player.DiscontinuityReason Int
	) {
		scope.launch {
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
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		scope.launch {
			if (!isPlaying) {
				setCurrentActivity(null)
			} else {
				setCurrentActivity(player.currentMediaItem)
			}
		}
	}

	private suspend fun setCurrentActivity(mediaItem: MediaItem?) {
		try {
			val cock = preferenceManager.enableDiscordIntegration
			val balls = !preferenceManager.disableRpcWhenPowerSaving
				|| !powerManager.isPowerSaveMode
			if (cock && balls) {
				if (!client.isConnectionReady) {
					client.connect(preferenceManager.discordAppId)
				}

				val song = mediaItem?.let { songRepository.getSongById(it.mediaId) }
				val album = song?.albumId?.let { albumRepository.getAlbumById(it) }

				if (song != null) {
					val activity = createDiscordActivity(song = song, album = album)
					client.setActivity(activity)
					return
				}
			} else {
				if (client.isConnectionReady) {
					client.disconnect()
				}
			}

			client.clearActivity()
		} catch (ex: Exception) {
			Log.e(TAG, "could not set current activity", ex)
		}
	}

	private fun createDiscordActivity(song: DomainSong, album: DomainAlbum?): Activity {
		val nowMs = System.currentTimeMillis()
		val currentPosMs = player.currentPosition.coerceAtLeast(0L)
		val startSec = (nowMs - currentPosMs)
		val endSec = if (player.duration > 0) {
			(startSec + player.duration)
		} else {
			null
		}

		val largeImage = album?.musicBrainzId?.let { mbzId ->
			"https://coverartarchive.org/release/$mbzId/front-250"
		}

		return Activity(
			name = "Navic",
			activityType = ActivityType.LISTENING,
			state = song.artistName?.padEnd(2, INVISIBLE_CHARACTER),
			details = song.title.padEnd(2, INVISIBLE_CHARACTER),
			statusDisplayType = preferenceManager.discordStatusType.wrappedType,
			assets = ActivityAssets(
				largeUrl = null,
				largeImage = largeImage,
				largeText = song.albumTitle?.padEnd(2, INVISIBLE_CHARACTER),
				smallImage = null,
				smallText = null,
				smallUrl = null
			),
			timestamps = endSec?.let { ActivityTimestamps(startSec, endSec) }
		)
	}
}
