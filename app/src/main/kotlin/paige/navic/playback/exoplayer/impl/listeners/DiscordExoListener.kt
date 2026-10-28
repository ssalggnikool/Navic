package paige.navic.playback.exoplayer.impl.listeners

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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import night.milkyway.antisocialcord.DiscordRpcClient
import night.milkyway.antisocialcord.model.Activity
import night.milkyway.antisocialcord.model.ActivityAssets
import night.milkyway.antisocialcord.model.ActivityTimestamps
import night.milkyway.antisocialcord.model.ActivityType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.discord.CoverArtUploadManager
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
	private val coverArtUploadManager: CoverArtUploadManager by inject()

	private val powerManager =
		context.getSystemService<PowerManager>()!! // idk why this would ever be null but yk

	companion object {
		const val DISCORD_APPLICATION_ID = "1554936646405984446"
		const val INVISIBLE_CHARACTER = '\u00A0'
		const val TAG = "ExoDiscordIntegration"
	}

	private val client = DiscordRpcClient(context)
	private val scope = CoroutineScope(Dispatchers.Main)
	private val mutex = Mutex()
	private val powerSaveChangedFlow = callbackFlow {
		val receiver = object : BroadcastReceiver() {
			override fun onReceive(p0: Context?, p1: Intent?) {
				launch {
					trySend(powerManager.isPowerSaveMode)
				}
			}
		}

		context.registerReceiver(
			receiver,
			IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
		)

		awaitClose { context.unregisterReceiver(receiver) }
	}.distinctUntilChanged()

	init {
		launch {
			merge(
				snapshotFlow { preferenceManager.enableDiscordIntegration },
				snapshotFlow { preferenceManager.disableRpcWhenPowerSaving },
				snapshotFlow { preferenceManager.discordStatusType },
				snapshotFlow { preferenceManager.discordAppId },
				powerSaveChangedFlow
			).collect {
				setCurrentActivity(player.currentMediaItem)
			}
		}
	}

	private fun launch(block: suspend CoroutineScope.() -> Unit) {
		scope.launch(block = block)
	}

	override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
		launch {
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
		launch {
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
		launch {
			if (!isPlaying) {
				setCurrentActivity(null)
			} else {
				setCurrentActivity(player.currentMediaItem)
			}
		}
	}

	private fun isIntegrationEnabled() = preferenceManager.enableDiscordIntegration
		&& (!preferenceManager.disableRpcWhenPowerSaving
		|| !powerManager.isPowerSaveMode)

	private suspend fun setCurrentActivity(mediaItem: MediaItem?) = mutex.withLock {
		try {
			if (isIntegrationEnabled()) {
				if (!client.isConnectionReady && mediaItem != null) {
					client.connect(preferenceManager.discordAppId)
				}

				if ((!player.isPlaying || mediaItem == null) && client.isConnectionReady) {
					client.clearActivity()
					return@withLock
				}

				val song = mediaItem?.let { songRepository.getSongById(it.mediaId) }
				val album = song?.albumId?.let { albumRepository.getAlbumById(it) }

				if (song != null) {
					val activity = createDiscordActivity(song = song, album = album)
					client.setActivity(activity)
					return@withLock
				}
			}
			client.clearActivity()

			if (client.isConnectionReady && !isIntegrationEnabled()) {
				client.setActivity(null) {
					client.disconnect()
				}
			}
		} catch (ex: Exception) {
			Log.e(TAG, "could not set current activity", ex)
		}
	}

	private suspend fun createDiscordActivity(song: DomainSong, album: DomainAlbum?): Activity {
		val nowMs = System.currentTimeMillis()
		val currentPosMs = player.currentPosition.coerceAtLeast(0L)
		val startSec = (nowMs - currentPosMs)
		val endSec = if (player.duration > 0) {
			(startSec + player.duration)
		} else {
			null
		}

		val largeImage = (song.coverArtId ?: album?.coverArtId)
			?.let { coverArtUploadManager.getPublicUrl(it) }

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
