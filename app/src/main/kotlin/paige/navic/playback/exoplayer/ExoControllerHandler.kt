/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.database.dao.AlbumDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.manager.playback.AudioGainManager
import paige.navic.domain.manager.playback.StreamCacheManager
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.DownloadManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.connectivity.ConnectivityManager
import paige.navic.domain.model.DomainExplicitStatus
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.settings.ExplicitContentPlayback
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource.Companion.buildCoverUri
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource.Companion.buildSongUri
import paige.navic.ui.core.PlayerUiState
import paige.navic.util.Logger
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@androidx.annotation.OptIn(UnstableApi::class)
class ExoControllerHandler(
	private val context: Context,
	private val controller: MediaController,
	private val state: MutableStateFlow<PlayerUiState>,
	private val preferenceManager: PreferenceManager,
	private val downloadManager: DownloadManager,
	private val sessionManager: SessionManager,
	private val audioGainManager: AudioGainManager,
	private val streamCacheManager: StreamCacheManager,
	private val connectivityManager: ConnectivityManager,
	private val songForId: (String) -> DomainSong?,
	private val rememberSong: (DomainSong) -> Unit,
	private val albumDao: AlbumDao,
	private val requestAutoFill: () -> Unit
) : Player.Listener {
	private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
	private var progressJob: Job? = null
	private var loadingCollectionId: String? = null

	private fun startProgressLoop() {
		progressJob?.cancel()
		progressJob = scope.launch {
			while (controller.isPlaying) {
				val duration = controller.effectiveDurationMs()
				if (duration > 0) {
					val progress =
						(controller.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
					state.update { it.copy(progress = progress) }
				}
				delay(200.milliseconds)
			}
		}
	}

	private fun updatePlaybackProperties(tracks: Tracks) {
		val audioGroups =
			tracks.groups.firstOrNull { it.type == C.TRACK_TYPE_AUDIO && it.isSelected }
		if (audioGroups != null) {
			for (i in 0 until audioGroups.length) {
				if (audioGroups.isTrackSelected(i)) {
					val format = audioGroups.getTrackFormat(i)
					Logger.i("MediaPlayer", "active track format: $format")

					scope.launch(Dispatchers.IO) {
						// i don't like this but oh well
						val currentSong = state.value.currentSong ?: return@launch
						val songMimeType = currentSong.mimeType
						val isOriginalFile =
							format.sampleMimeType == songMimeType || format.containerMimeType == songMimeType
						val fallbackBitrate = if (isOriginalFile) {
							currentSong.bitRate?.times(1000)
						} else {
							withContext(Dispatchers.Main) {
								getEstimateBitrate()
							}
						}

						state.update { current ->
							current.copy(
								playbackBitrate = format.bitrate.takeIf { it != Format.NO_VALUE }
									?: fallbackBitrate,
								playbackSampleRate = format.sampleRate.takeIf { it != Format.NO_VALUE },
								playbackMimeType = format.sampleMimeType,
								playbackBitDepth = when (format.pcmEncoding) {
									C.ENCODING_PCM_8BIT -> 8
									C.ENCODING_PCM_16BIT -> 16
									C.ENCODING_PCM_24BIT -> 24
									C.ENCODING_PCM_32BIT, C.ENCODING_PCM_FLOAT -> 32
									else -> -1
								}
							)
						}
					}
					break
				}
			}
		}
	}

	/**
	 * strategically skip around in the queue until the
	 * current song is available while avoiding infinite
	 * loops
	 *
	 * this **INTENTIONALLY** does not check for if the song
	 * is not downloaded and if the device is offline
	 *
	 * this used to check for that but because there have
	 * been cases where the device is falsely identified
	 * as being offline that's no longer the case, so we
	 * just try to play the song anyway
	 */
	private fun skipUnavailableSong() {
		val currentSong = state.value.currentSong ?: return
		if (!currentSong.isExplicit()) return
		Logger.i("MediaPlayer", "trying to skip unavailable song")
		val queue = state.value.queue
		val currentIdx = queue.indexOf(currentSong)

		// look for the next available song, wrapping around, but stop before
		// we loop back past our own starting point
		val nextAvailableIdx = (1..queue.size)
			.map { offset -> (currentIdx + offset) % queue.size }
			.firstOrNull { index -> !queue[index].isExplicit() }

		if (nextAvailableIdx == null) {
			Logger.i(
				"MediaPlayer",
				"pausing because this song is unavailable and there isn't anything to skip to"
			)
			controller.pause()
			return
		}

		if (nextAvailableIdx <= currentIdx) {
			Logger.i(
				"MediaPlayer",
				"skipping and pausing because the last song in the queue was unavailable"
			)
			controller.seekTo(nextAvailableIdx, 0L)
			controller.pause()
		} else {
			// just skip to the next song
			controller.seekTo(nextAvailableIdx, 0L)
		}
	}

	fun updatePlaybackState() {
		val index = controller.currentMediaItemIndex
		val queue = (0 until controller.mediaItemCount)
			.mapNotNull { controller.getMediaItemAt(it).mediaId.let(songForId) }
		val currentSong = if (index == C.INDEX_UNSET) null else queue.getOrNull(index)

		val derivedCollection = currentSong?.let { song ->
			val stateCollection = state.value.currentCollection

			if (stateCollection?.id == song.albumId.toString()) {
				stateCollection
			} else {
				refreshCollection(song.albumId.toString())
				null
			}
		}

		state.update { current ->
			current.copy(
				queue = queue,
				currentIndex = index,
				currentSong = currentSong,
				currentCollection = derivedCollection ?: current.currentCollection,
				progress = if (index == C.INDEX_UNSET) 0f else current.progress,
				isPaused = (!controller.playWhenReady || controller.playbackState == Player.STATE_ENDED),
				isShuffleEnabled = controller.shuffleModeEnabled,
				repeatMode = controller.repeatMode
			)
		}
		applyAudioGain()
		updateProgress()
	}

	private fun refreshCollection(albumId: String) {
		if (loadingCollectionId == albumId) return
		loadingCollectionId = albumId

		scope.launch {
			runCatching {
				val album = albumDao.getAlbumById(albumId)

				state.update { it.copy(currentCollection = album?.toDomainModel()) }
			}.onFailure {
				loadingCollectionId = null
			}
		}
	}

	override fun onMediaItemTransition(
		mediaItem: MediaItem?,
		reason: @Player.MediaItemTransitionReason Int
	) {
		updatePlaybackState()
		skipUnavailableSong()
		requestAutoFill()
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		if (isPlaying) {
			startProgressLoop()
		}

		val currentSong = state.value.currentSong
		val displayArtist = currentSong?.artists?.joinToString { it.name }
			?.ifBlank { currentSong.artistName }

		val intent =
			Intent("${context.packageName}.NOW_PLAYING_UPDATED").apply {
				setPackage(context.packageName)
				putExtra("isPlaying", isPlaying)
				putExtra(
					"title",
					currentSong?.title ?: "Unknown song"
				)
				putExtra(
					"artist",
					displayArtist ?: "Unknown artist"
				)
				putExtra(
					"artUrl",
					currentSong?.coverArtId?.let { id ->
						downloadManager.getCoverArtFile(id)
							?.toUri()
							?.toString()
							?: sessionManager.getCoverArtUrl(id)
					})
			}

		context.sendBroadcast(intent)
		updatePlaybackState()
	}

	override fun onPlaybackStateChanged(playbackState: Int) {
		state.update { it.copy(isLoading = playbackState == Player.STATE_BUFFERING) }
		updatePlaybackState()
	}

	override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
		state.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
	}

	override fun onRepeatModeChanged(repeatMode: Int) {
		state.update { it.copy(repeatMode = repeatMode) }
	}

	override fun onTracksChanged(tracks: Tracks) {
		updatePlaybackProperties(tracks)
	}

	override fun onTimelineChanged(timeline: Timeline, reason: Int) {
		updatePlaybackState()
	}

	override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
		val currentSong = controller.currentMediaItem?.mediaId?.let(songForId) ?: return
		if (currentSong.id.startsWith("radio_")) {
			val rawTitle = mediaMetadata.title?.toString()?.takeIf { it.isNotBlank() }
			val rawArtist = mediaMetadata.artist?.toString()
				?.takeIf { it.isNotBlank() && it != "Live Radio" }
				?: mediaMetadata.subtitle?.toString()
					?.takeIf { it.isNotBlank() && it != "Live Radio" }

			val updatedSong = if (rawTitle != null) {
				val parts = rawTitle.split(" - ", limit = 2)
				if (parts.size == 2) {
					currentSong.copy(
						artistName = parts[0].trim(),
						title = parts[1].trim()
					)
				} else if (rawArtist != null) {
					currentSong.copy(
						title = rawTitle,
						artistName = rawArtist
					)
				} else {
					currentSong.copy(
						title = rawTitle,
						artistName = "Live Radio"
					)
				}
			} else {
				currentSong
			}

			if (updatedSong != currentSong) {
				rememberSong(updatedSong)
				updatePlaybackState()
			}
		}
	}

	private fun applyAudioGain() = audioGainManager.apply {
		setAmplifierValues(preferenceManager.rgAmpGain, preferenceManager.ampGain)

		if (preferenceManager.replayGainMode != ReplayGainMode.Off) {
			val currentSong = state.value.currentSong
			val replayGain = currentSong?.replayGain

			if (replayGain != null) {
				setReplayGainMetadata(replayGain)

				if (preferenceManager.replayGainMode != ReplayGainMode.Dynamic) {
					applyGainMode(preferenceManager.replayGainMode)
				} else {
					if (state.value.queue.all { it.albumId == currentSong.albumId }) {
						applyGainMode(ReplayGainMode.Album)
					} else {
						applyGainMode(ReplayGainMode.Track)
					}
				}

				return@apply
			}

			setReplayGainMetadata(null)
			return@apply
		}
		resetGain()
	}

	private suspend fun getEstimateBitrate(): Int? {
		val duration = (controller.currentMediaItem?.mediaMetadata?.durationMs?.div(1000))
			?: return null

		state.value.currentSong?.let { song ->
			val fileSize = runCatching {
				downloadManager.getDownloadedFilePath(song.id)?.toFile()
					?.length()
					?.takeIf { it > 0L }
			}.getOrNull()
				?: streamCacheManager.getSongFileSize(song.id)
				?: return NavicDataSource.getCurrentQuality(
					preferenceManager,
					connectivityManager
				).first.takeIf { it > 0 }?.times(1000)

			if (duration <= 0 || fileSize <= 0) return null
			return ((fileSize * 8) / duration).toInt()
		}
		return null
	}

	private fun String.toFile() = File(this)

	fun setPlaybackSpeed(value: Float) {
		controller.playbackParameters = PlaybackParameters(
			value,
			if (state.value.preservePitch) 1f else value
		)
		state.update { it.copy(playbackSpeed = value) }
	}

	fun setPreservePitch(value: Boolean) {
		controller.playbackParameters = PlaybackParameters(
			state.value.playbackSpeed,
			if (value) 1f else state.value.playbackSpeed
		)
		state.update { it.copy(preservePitch = value) }
	}

	fun seek(normalized: Float) {
		val target = (controller.effectiveDurationMs() * normalized).toLong()
		controller.seekTo(target)
		state.update { it.copy(progress = normalized) }
	}

	private fun updateProgress() {
		val duration = controller.effectiveDurationMs()
		if (duration > 0) {
			val pos = controller.currentPosition
			val progress = (pos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
			state.update { it.copy(progress = progress) }
		}
	}

	private fun DomainSong.isExplicit(): Boolean {
		return this.explicitStatus == DomainExplicitStatus.Explicit
			&& preferenceManager.explicitContentPlayback != ExplicitContentPlayback.Allowed
	}
}

fun Player.effectiveDurationMs(): Long {
	return duration.takeIf { it > 0 }
		?: currentMediaItem?.mediaMetadata?.durationMs
		?: 0L
}

fun DomainSong.toMediaItem(): MediaItem {
	val displayArtist = artists.joinToString { it.name }.ifBlank { artistName }
	val albumArtistName = albumArtists.joinToString { it.name }.ifBlank { artistName }

	val metadataBuilder = MediaMetadata.Builder()
		.apply {
			setTitle(title)
			setSubtitle(displayArtist)
			setArtist(displayArtist)
			setAlbumArtist(albumArtistName)
			setAlbumTitle(albumTitle)
			setDurationMs(duration.inWholeMilliseconds)
			setArtworkUri(coverArtId?.buildCoverUri())
			setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
		}

	val metadata = metadataBuilder.build()

	val uri = when {
		id.startsWith("radio_") && !filePath.isNullOrEmpty() -> filePath.toUri()
		else -> id.buildSongUri()
	}

	val builder = MediaItem.Builder()
		.setUri(uri)
		.setMediaId(id)
		.setMediaMetadata(metadata)

	if (id.startsWith("radio_")) {
		builder.setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build())
	}

	return builder.build()
}
