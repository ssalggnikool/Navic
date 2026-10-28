package paige.navic.playback.exoplayer.impl.listeners

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import paige.navic.database.entity.SyncActionType
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.session.connectivity.ReachabilityFlow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class ScrobbleExoListener(
    private val player: Player,
    val reachability: ReachabilityFlow,
    val syncManager: SyncManager,
    val sessionManager: SessionManager,
    val preferenceManager: PreferenceManager
) : Player.Listener {
	private var currentMediaId: String? = null
	private var hasScrobbledCurrent = false
	private var hasSentNowPlaying = false
	private var progressJob: Job? = null
	private var accumulatedPlayTime: Long = 0
	private val scope = CoroutineScope(Dispatchers.IO)

	init {
		player.addListener(this)
	}

	private fun scrobbleNowPlaying(songId: String?) {
		if (!preferenceManager.enableScrobbling || songId == null) return

		if (!reachability.isReachable.value) return

		scope.launch(Dispatchers.IO) {
			try {
				sessionManager.api.scrobble(songId, submission = false)
			} catch (_: Exception) {
			}
		}
	}

	private fun scrobbleSubmission(songId: String?) {
		if (!preferenceManager.enableScrobbling || songId == null) return

		scope.launch(Dispatchers.IO) {
			val currentTime = Clock.System.now()

			if (reachability.isReachable.value) {
				try {
					sessionManager.api.scrobble(songId, submission = true, time = currentTime)
				} catch (_: Exception) {
					syncManager.enqueueAction(SyncActionType.SCROBBLE, songId, currentTime)
				}
			} else {
				syncManager.enqueueAction(SyncActionType.SCROBBLE, songId, currentTime)
			}
		}
	}

	fun hasTransitioned(mediaItem: MediaItem?) {
		currentMediaId = mediaItem?.mediaId
		hasScrobbledCurrent = false
		hasSentNowPlaying = false
		accumulatedPlayTime = 0
		progressJob?.cancel()
		if (player.isPlaying) {
			startProgressTracker()
			scrobbleNowPlaying(mediaItem?.mediaId)
			hasSentNowPlaying = true
		}
	}

	override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) =
		hasTransitioned(mediaItem)

	private fun startProgressTracker() {
		progressJob?.cancel()
		progressJob = scope.launch(Dispatchers.Main) {
			var lastTickTime = Clock.System.now().toEpochMilliseconds()

			while (isActive) {
				val now = Clock.System.now().toEpochMilliseconds()
				val timePassed = now - lastTickTime
				lastTickTime = now

				accumulatedPlayTime += timePassed

				checkProgress()
                delay(2.seconds)
			}
		}
	}

	private fun checkProgress() {
		if (hasScrobbledCurrent) return

		val duration = player.duration
		if (duration <= 0) return

		val percent = accumulatedPlayTime.toFloat() / duration.toFloat()
		val playedEnoughPercent = percent >= preferenceManager.scrobblePercentage
		val isValidSong = duration >= preferenceManager.minDurationToScrobble * 1000

		if (isValidSong && playedEnoughPercent) {
			scrobbleSubmission(currentMediaId)
			hasScrobbledCurrent = true
		}
	}

	override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
	) {
		val isSeekOrRepeat = reason == Player.DISCONTINUITY_REASON_SEEK ||
			reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION

		val jumpedToStart = newPosition.positionMs < 5000L
		val wasFurtherAlong = oldPosition.positionMs > 10000L

		if (isSeekOrRepeat && jumpedToStart && wasFurtherAlong) {
			hasTransitioned(player.currentMediaItem)
		}
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		if (isPlaying) {
			startProgressTracker()
			if (!hasSentNowPlaying) {
				scrobbleNowPlaying(currentMediaId)
				hasSentNowPlaying = true
			}
		} else {
			progressJob?.cancel()
		}
	}

	fun release() {
		player.removeListener(this)
	}
}
