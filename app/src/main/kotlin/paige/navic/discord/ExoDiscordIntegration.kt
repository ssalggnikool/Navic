/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.discord

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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.manager.PreferenceManager
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

	companion object {
		const val DISCORD_APPLICATION_ID = "1554936646405984446"
		const val INVISIBLE_CHARACTER = '\u00A0'
		const val TAG = "ExoDiscordIntegration"
	}

	private val client = DiscordRpcClient(context)
	private val scope = CoroutineScope(Dispatchers.Main)
	private val mutex = Mutex()

	fun isIntegrationEnabled(): Boolean {
		return preferenceManager.enableDiscordIntegration
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
							client.connect(preferenceManager.discordAppId)
						}

						val song = mediaItem?.let { songRepository.getSongById(it.mediaId) }
						val album = song?.albumId?.let { albumRepository.getAlbumById(it) }

						if (song != null) {
							val activity = createDiscordActivity(song = song, album = album)
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
			statusDisplayType = StatusDisplayType.STATE,
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
