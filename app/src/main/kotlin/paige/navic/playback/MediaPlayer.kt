/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

import android.app.Application
import android.content.Intent
import android.os.Bundle
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
import androidx.media3.session.SessionCommand
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainExplicitStatus
import paige.navic.domain.model.DomainRadio
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongCollection
import paige.navic.domain.model.settings.ExplicitContentPlayback
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.domain.repository.PlayerStateRepository
import paige.navic.domain.repository.SongRepository
import paige.navic.playback.exoplayer.ExoStateHolder
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource.Companion.buildCoverUri
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource.Companion.buildSongUri
import paige.navic.ui.core.PlayerUiState
import paige.navic.util.Logger
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@androidx.annotation.OptIn(UnstableApi::class)
class MediaPlayer(
	private val stateRepository: PlayerStateRepository,
	private val songRepository: SongRepository,
	private val downloadManager: DownloadManager,
	private val preferenceManager: PreferenceManager,
	private val audioGainManager: AudioGainManager,
	private val application: Application,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager,
	private val streamCacheManager: StreamCacheManager,
	private val connectivityManager: ConnectivityManager
) {
	private val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
	private val scope = CoroutineScope(Dispatchers.Main)

	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null

	private var loadingCollectionId: String? = null

	private var pendingSyncState: PlayerUiState? = null

	init {
		connectService()
		launch {
			restoreState()
		}
		observeAndSaveState()
	}

	fun playNow(song: DomainSong) {
		playNowInternal(listOf(song), 0)
	}

	fun playNow(collection: DomainSongCollection, startIndex: Int = 0) {
		playNowInternal(collection.sortedSongs(), startIndex)
	}

	fun playNow(songs: List<DomainSong>, startIndex: Int = 0) {
		playNowInternal(songs, startIndex)
	}

	private fun DomainSongCollection.sortedSongs(): List<DomainSong> =
		if (this is DomainAlbum) songs.sortedWith(
			compareBy({ it.discNumber }, { it.trackNumber })
		) else songs

	private fun playNowInternal(songs: List<DomainSong>, startIndex: Int) =
		this@MediaPlayer.launch {
			val shuffle = uiState.value.isShuffleEnabled
			val index = startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0))
			val order =
				if (shuffle && songs.isNotEmpty()) shuffledOrder(songs.size, index) else emptyList()
			val items = withContext(Dispatchers.Default) { songs.map { it.toMediaItem() } }

			_uiState.update { state ->
				state.copy(
					queue = songs,
					shuffleOrder = order,
					currentIndex = if (songs.isEmpty()) -1 else index,
					currentSong = songs.getOrNull(index),
					progress = 0f
				)
			}

			controller?.let { player ->
				if (songs.isEmpty()) {
					player.clearMediaItems()
				} else {
					player.setMediaItems(items, index, 0L)
					if (order.isNotEmpty()) pushShuffleOrder(order)
					player.shuffleModeEnabled = shuffle
					player.prepare()
					player.play()
				}
			}
			checkAndAutoFillQueue()
		}

	fun togglePlay() {
		if (!uiState.value.isPaused) {
			pause()
		} else {
			resume()
		}
	}

	private fun isExplicit(song: DomainSong): Boolean {
		return song.explicitStatus == DomainExplicitStatus.Explicit
			&& preferenceManager.explicitContentPlayback != ExplicitContentPlayback.Allowed
	}

	private fun isValidShuffleOrder(order: List<Int>, size: Int): Boolean {
		if (order.size != size) return false
		val seen = BooleanArray(size)
		for (index in order) {
			if (index !in 0 until size || seen[index]) return false
			seen[index] = true
		}
		return true
	}

	private fun shuffledOrder(size: Int, first: Int): List<Int> {
		if (size <= 0) return emptyList()
		val firstIndex = first.coerceIn(0, size - 1)
		return listOf(firstIndex) + (0 until size).filter { it != firstIndex }.shuffled()
	}

	private fun ensureShuffleOrder() {
		val state = _uiState.value
		if (!state.isShuffleEnabled || state.queue.isEmpty()) return

		val order = state.shuffleOrder.takeIf { isValidShuffleOrder(it, state.queue.size) }
			?: shuffledOrder(state.queue.size, state.currentIndex.coerceAtLeast(0))
		if (order != state.shuffleOrder) {
			_uiState.update { it.copy(shuffleOrder = order) }
		}
		pushShuffleOrder(order)
	}

	private fun pushShuffleOrder(order: List<Int> = _uiState.value.shuffleOrder) {
		val controller = controller ?: return
		if (order.isEmpty() || !isValidShuffleOrder(order, order.size)) return

		controller.sendCustomCommand(
			SessionCommand(ExoStateHolder.COMMAND_SET_SHUFFLE_ORDER, Bundle.EMPTY),
			Bundle().apply {
				putIntArray(ExoStateHolder.KEY_SHUFFLE_ORDER, order.toIntArray())
			}
		)
	}

	private fun appendedShuffleOrder(
		state: PlayerUiState,
		songs: List<DomainSong>
	): List<Int> {
		if (!state.isShuffleEnabled || songs.isEmpty()) return emptyList()
		if (state.queue.isEmpty()) return shuffledOrder(songs.size, 0)

		val base = state.shuffleOrder.takeIf { isValidShuffleOrder(it, state.queue.size) }
			?: shuffledOrder(state.queue.size, state.currentIndex.coerceAtLeast(0))
		return base + songs.indices.map { state.queue.size + it }
	}

	private fun insertedShuffleOrder(
		state: PlayerUiState,
		insertAt: Int,
		songs: List<DomainSong>
	): List<Int> {
		if (!state.isShuffleEnabled || songs.isEmpty()) return emptyList()
		if (state.queue.isEmpty()) return shuffledOrder(songs.size, 0)

		val base = state.shuffleOrder.takeIf { isValidShuffleOrder(it, state.queue.size) }
			?: shuffledOrder(state.queue.size, state.currentIndex.coerceAtLeast(0))
		val shifted = base.map { if (it >= insertAt) it + songs.size else it }
		val currentDisplay = base.indexOf(state.currentIndex)
		val displayInsert = (if (currentDisplay >= 0) currentDisplay + 1 else shifted.size)
			.coerceIn(0, shifted.size)
		val newIndices = songs.indices.map { insertAt + it }

		return shifted.toMutableList().apply { addAll(displayInsert, newIndices) }
	}

	private fun checkAndAutoFillQueue() {
		if (!preferenceManager.autoFillQueue) return

		val state = uiState.value
		if (state.activeQueue.isEmpty()) return

		val remainingCount = state.activeQueue.size - state.activeIndex

		if (remainingCount <= 1) {
			scope.launch {
				val randomSongs = songRepository.getRandomSongsList(1)
				addToQueue(randomSongs, notify = false)
			}
		}
	}

	fun checkDuplicate(
		songId: String,
		preferenceManager: PreferenceManager
	): Boolean {
		return !preferenceManager.shushQueueDuplicateDialog && uiState.value.queue.any { it.id == songId }
	}

	private suspend fun restoreState() {
		val savedState = stateRepository.state.first()
			.copy(isPaused = true, isLoading = false)
			.withValidShuffleOrder()
		_uiState.value = savedState
		syncPlayerWithState(savedState)
		checkAndAutoFillQueue()
	}

	private fun PlayerUiState.withValidShuffleOrder(): PlayerUiState {
		val order = if (isShuffleEnabled && queue.isNotEmpty()) {
			shuffleOrder.takeIf { isValidShuffleOrder(it, queue.size) }
				?: shuffledOrder(queue.size, currentIndex.coerceAtLeast(0))
		} else {
			emptyList()
		}
		return if (order == shuffleOrder) this else copy(shuffleOrder = order)
	}

	private fun observeAndSaveState() = scope.launch {
		uiState
			.distinctUntilChanged { old, new ->
				old.currentIndex == new.currentIndex &&
					old.queue == new.queue &&
					old.shuffleOrder == new.shuffleOrder &&
					old.isPaused == new.isPaused &&
					old.repeatMode == new.repeatMode &&
					old.isShuffleEnabled == new.isShuffleEnabled
			}
			.collect { state ->
				stateRepository.setState(state)
			}

	}

	// just because I hate writing boilerplate every single line
	private fun launch(
		context: CoroutineContext = Dispatchers.Main,
		block: suspend CoroutineScope.() -> Unit
	) {
		scope.launch(
			context = context,
			block = block
		)
	}

	private fun connectService() = launch {
		val sessionToken = ExoStateHolder.newSessionToken(application)
		controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
		controllerFuture?.addListener({
			controller = controllerFuture?.get()
			setupController()
		}, MoreExecutors.directExecutor())
	}

	private fun setupController() = launch {
		controller?.apply {
			addListener(object : Player.Listener {
				override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
					updatePlaybackState()
					skipUnavailableSong()
					checkAndAutoFillQueue()
				}

				override fun onIsPlayingChanged(isPlaying: Boolean) {
					if (isPlaying) {
						startProgressLoop()
					}

					val currentSong = _uiState.value.currentSong
					val displayArtist = currentSong?.artists?.joinToString { it.name }
						?.ifBlank { currentSong.artistName }

					val intent =
						Intent("${application.packageName}.NOW_PLAYING_UPDATED").apply {
							setPackage(application.packageName)
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

					application.sendBroadcast(intent)
					updatePlaybackState()
				}

				override fun onPlaybackStateChanged(playbackState: Int) {
					_uiState.update { it.copy(isLoading = playbackState == Player.STATE_BUFFERING) }
					updatePlaybackState()
				}

				override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
					if (shuffleModeEnabled) {
						val state = _uiState.value
						val order = if (!state.isShuffleEnabled || !isValidShuffleOrder(
								state.shuffleOrder,
								state.queue.size
							)
						) {
							if (state.queue.isEmpty()) emptyList()
							else shuffledOrder(
								state.queue.size,
								state.currentIndex.coerceAtLeast(0)
							)
						} else {
							state.shuffleOrder
						}
						_uiState.update { it.copy(isShuffleEnabled = true, shuffleOrder = order) }
						pushShuffleOrder(order)
					} else {
						_uiState.update {
							it.copy(
								isShuffleEnabled = false,
								shuffleOrder = emptyList()
							)
						}
					}
				}

				override fun onRepeatModeChanged(repeatMode: Int) {
					_uiState.update { it.copy(repeatMode = repeatMode) }
				}

				override fun onTracksChanged(tracks: Tracks) {
					updatePlaybackProperties(tracks)
				}

				override fun onTimelineChanged(timeline: Timeline, reason: Int) {
					updatePlaybackState()
				}

				override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
					val currentSong = _uiState.value.currentSong ?: return
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
							_uiState.update { state ->
								val updatedQueue = state.queue.map { song ->
									if (song.id == currentSong.id) updatedSong else song
								}
								state.copy(
									currentSong = updatedSong,
									queue = updatedQueue
								)
							}
							updatePlaybackState()
						}
					}
				}
			})

			downloadManager.allDownloads.first()
			pendingSyncState?.let { state ->
				syncPlayerWithState(state)
				pendingSyncState = null
			}
			checkAndAutoFillQueue()
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
		val state = _uiState.value
		val queue = state.activeQueue
		val currentIdx = state.activeIndex
		if (currentIdx !in queue.indices) return
		val currentSong = queue[currentIdx]
		if (!isExplicit(currentSong)) return
		Logger.i("MediaPlayer", "trying to skip unavailable song")

		// look for the next available song, wrapping around, but stop before
		// we loop back past our own starting point
		val nextAvailableIdx = (1..queue.size)
			.map { offset -> (currentIdx + offset) % queue.size }
			.firstOrNull { index -> !isExplicit(queue[index]) }

		if (nextAvailableIdx == null) {
			Logger.i(
				"MediaPlayer",
				"pausing because this song is unavailable and there isn't anything to skip to"
			)
			controller?.pause()
			return
		}

		val nextNormalIdx =
			if (state.isShuffled) state.shuffleOrder[nextAvailableIdx] else nextAvailableIdx

		if (nextAvailableIdx <= currentIdx) {
			Logger.i(
				"MediaPlayer",
				"skipping and pausing because the last song in the queue was unavailable"
			)
			controller?.seekTo(nextNormalIdx, 0L)
			controller?.pause()
		} else {
			// just skip to the next song
			controller?.seekTo(nextNormalIdx, 0L)
		}
	}

	private fun refreshCurrentCollection(albumId: String) {
		if (loadingCollectionId == albumId) return
		loadingCollectionId = albumId

		scope.launch {
			runCatching {
				val album = albumDao.getAlbumById(albumId)

				_uiState.update { it.copy(currentCollection = album?.toDomainModel()) }
			}.onFailure {
				loadingCollectionId = null
			}
		}
	}

	private fun updatePlaybackState() {
		val controller = controller ?: return
		val index = controller.currentMediaItemIndex
		if (index == C.INDEX_UNSET) return

		val currentSong = _uiState.value.queue.getOrNull(index)

		val derivedCollection = currentSong?.let { song ->
			val stateCollection = _uiState.value.currentCollection

			if (stateCollection?.id == song.albumId.toString()) {
				stateCollection
			} else {
				refreshCurrentCollection(song.albumId.toString())
				null
			}
		}

		_uiState.update { state ->
			state.copy(
				currentIndex = index,
				currentSong = currentSong,
				currentCollection = derivedCollection ?: state.currentCollection,
				isPaused = (!controller.playWhenReady || controller.playbackState == Player.STATE_ENDED),
				isShuffleEnabled = controller.shuffleModeEnabled,
				repeatMode = controller.repeatMode
			)
		}
		applyAudioGain()
		updateProgress()
	}

	private fun applyAudioGain() = audioGainManager.apply {
		setAmplifierValues(preferenceManager.rgAmpGain, preferenceManager.ampGain)

		if (preferenceManager.replayGainMode != ReplayGainMode.Off) {
			val currentSong = _uiState.value.currentSong
			val replayGain = currentSong?.replayGain

			if (replayGain != null) {
				setReplayGainMetadata(replayGain)

				if (preferenceManager.replayGainMode != ReplayGainMode.Dynamic) {
					applyGainMode(preferenceManager.replayGainMode)
				} else {
					if (_uiState.value.queue.all { it.albumId == currentSong.albumId }) {
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

	fun syncPlayerWithState(state: PlayerUiState) = launch {
		val player = controller

		if (player == null) {
			pendingSyncState = state
			return@launch
		}

		if (state.queue.isEmpty() || player.mediaItemCount > 0) {
			updatePlaybackState()
			ensureShuffleOrder()
			return@launch
		}

		val mediaItems = withContext(Dispatchers.Default) {
			state.queue.map { it.toMediaItem() }
		}

		val index = if (state.currentIndex in mediaItems.indices) {
			state.currentIndex
		} else {
			0
		}
		val order = if (state.isShuffleEnabled && mediaItems.isNotEmpty()) {
			state.shuffleOrder.takeIf { isValidShuffleOrder(it, mediaItems.size) }
				?: shuffledOrder(mediaItems.size, index)
		} else {
			emptyList()
		}
		if (order != state.shuffleOrder) {
			_uiState.update { it.copy(shuffleOrder = order) }
		}

		player.apply {
			setMediaItems(mediaItems)
			if (order.isNotEmpty()) pushShuffleOrder(order)

			shuffleModeEnabled = state.isShuffleEnabled
			repeatMode = state.repeatMode
			playbackParameters = PlaybackParameters(state.playbackSpeed)
		}

		val songDurationMs = state.queue.getOrNull(index)?.duration?.inWholeMilliseconds ?: 0L

		val position = if (songDurationMs > 0) {
			(state.progress * songDurationMs).toLong()
		} else {
			0L
		}

		player.seekTo(index, position)
		player.prepare()
		if (!state.isPaused) {
			player.play()
		}
	}

	private fun Player.effectiveDurationMs(): Long {
		return duration.takeIf { it > 0 }
			?: currentMediaItem?.mediaMetadata?.durationMs
			?: 0L
	}

	private fun startProgressLoop() = this@MediaPlayer.launch {
		while (controller?.isPlaying == true) {
			val player = controller ?: break
			val duration = player.effectiveDurationMs()
			if (duration > 0) {
				val progress =
					(player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
				_uiState.update { it.copy(progress = progress) }
			}
			delay(200.milliseconds)
		}
	}

	private fun updateProgress() = controller?.also { player ->
		val duration = player.effectiveDurationMs()
		if (duration > 0) {
			val pos = player.currentPosition
			val progress = (pos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
			_uiState.update { it.copy(progress = progress) }
		}
	}

	private suspend fun getEstimateBitrate(): Int? {
		val controller = controller ?: return null
		val duration =
			(controller.currentMediaItem?.mediaMetadata?.durationMs?.div(1000)) ?: return null

		_uiState.value.currentSong?.let { song ->
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
						val currentSong = _uiState.value.currentSong ?: return@launch
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

						_uiState.update { state ->
							state.copy(
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

	fun addToQueueSingle(song: DomainSong, notify: Boolean = true) = this@MediaPlayer.launch {
		val order = appendedShuffleOrder(_uiState.value, listOf(song))
		_uiState.update {
			it.copy(
				queue = it.queue + song,
				shuffleOrder = order,
				currentIndex = if (it.currentIndex == -1) 0 else it.currentIndex,
				currentSong = if (it.currentIndex == -1) song else it.currentSong
			)
		}
		controller?.addMediaItem(song.toMediaItem())
		if (order.isNotEmpty()) pushShuffleOrder(order)
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	fun addToQueue(collection: DomainSongCollection, notify: Boolean = true) {
		addToQueue(collection.sortedSongs(), notify)
	}

	fun addToQueue(songs: List<DomainSong>, notify: Boolean = true) = this@MediaPlayer.launch {
		if (songs.isEmpty()) return@launch
		val order = appendedShuffleOrder(_uiState.value, songs)
		val items = withContext(Dispatchers.Default) { songs.map { it.toMediaItem() } }
		_uiState.update {
			it.copy(
				queue = it.queue + songs,
				shuffleOrder = order,
				currentIndex = if (it.currentIndex == -1) 0 else it.currentIndex,
				currentSong = if (it.currentIndex == -1) songs.first() else it.currentSong
			)
		}
		controller?.addMediaItems(items)
		if (order.isNotEmpty()) pushShuffleOrder(order)
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	fun removeFromQueue(index: Int) = this@MediaPlayer.launch {
		val state = _uiState.value
		val normalIndex = if (state.isShuffled) {
			state.shuffleOrder.getOrNull(index) ?: return@launch
		} else {
			index
		}
		if (normalIndex !in state.queue.indices) return@launch

		val newQueue = state.queue.toMutableList().apply { removeAt(normalIndex) }
		val newIndex = when {
			state.currentIndex == -1 -> -1
			normalIndex < state.currentIndex -> state.currentIndex - 1
			normalIndex == state.currentIndex -> if (newQueue.isEmpty()) -1 else state.currentIndex.coerceAtMost(
				newQueue.size - 1
			)

			else -> state.currentIndex
		}
		val newOrder = if (state.isShuffleEnabled) {
			val filtered = state.shuffleOrder
				.filter { it != normalIndex }
				.map { if (it > normalIndex) it - 1 else it }
			filtered.takeIf { isValidShuffleOrder(it, newQueue.size) }
				?: if (newQueue.isEmpty()) emptyList()
				else shuffledOrder(newQueue.size, newIndex.coerceAtLeast(0))
		} else {
			emptyList()
		}

		_uiState.update {
			it.copy(
				queue = newQueue,
				shuffleOrder = newOrder,
				currentIndex = newIndex,
				currentSong = if (newIndex == -1) null else newQueue[newIndex]
			)
		}
		controller?.removeMediaItem(normalIndex)
		if (newOrder.isNotEmpty()) pushShuffleOrder(newOrder)
	}

	fun moveQueueItem(fromIndex: Int, toIndex: Int) = this@MediaPlayer.launch {
		val state = _uiState.value
		if (fromIndex !in state.activeQueue.indices || toIndex !in state.activeQueue.indices) {
			return@launch
		}
		if (fromIndex == toIndex) return@launch

		if (state.isShuffled) {
			val newOrder = state.shuffleOrder.toMutableList().apply {
				add(toIndex, removeAt(fromIndex))
			}
			_uiState.update { it.copy(shuffleOrder = newOrder) }
			pushShuffleOrder(newOrder)
			return@launch
		}

		val newQueue = state.queue.toMutableList().apply {
			val item = removeAt(fromIndex)
			add(toIndex, item)
		}
		val newIndex = when (state.currentIndex) {
			fromIndex -> toIndex
			in (fromIndex + 1)..toIndex -> state.currentIndex - 1
			in toIndex until fromIndex -> state.currentIndex + 1
			else -> state.currentIndex
		}
		_uiState.update {
			it.copy(
				queue = newQueue,
				shuffleOrder = emptyList(),
				currentIndex = newIndex,
				currentSong = if (newIndex == -1) null else newQueue[newIndex]
			)
		}
		controller?.moveMediaItem(fromIndex, toIndex)
		if (state.isShuffleEnabled) ensureShuffleOrder()
	}

	fun clearQueue() = this@MediaPlayer.launch {
		_uiState.update {
			it.copy(
				queue = emptyList(),
				shuffleOrder = emptyList(),
				currentSong = null,
				currentIndex = -1,
				progress = 0f
			)
		}
		controller?.clearMediaItems()
	}

	fun playAt(index: Int) = this@MediaPlayer.launch {
		val state = _uiState.value
		val normalIndex = if (state.isShuffled) {
			state.shuffleOrder.getOrNull(index) ?: return@launch
		} else {
			index
		}
		controller?.let { player ->
			if (normalIndex in 0 until player.mediaItemCount) {
				player.seekTo(normalIndex, 0L)
				player.play()
			}
		}
	}

	fun playNextSingle(song: DomainSong) = this@MediaPlayer.launch {
		val state = _uiState.value
		val insertAt = (state.currentIndex + 1).coerceIn(0, state.queue.size)
		val newQueue = state.queue.toMutableList().apply { add(insertAt, song) }
		val newOrder = insertedShuffleOrder(state, insertAt, listOf(song))

		_uiState.update {
			it.copy(
				queue = newQueue,
				shuffleOrder = newOrder,
				currentIndex = if (it.currentIndex == -1) 0 else it.currentIndex,
				currentSong = if (it.currentIndex == -1) song else it.currentSong
			)
		}
		controller?.addMediaItem(
			insertAt,
			withContext(Dispatchers.Default) { song.toMediaItem() }
		)
		if (newOrder.isNotEmpty()) pushShuffleOrder(newOrder)
		snackBarManager.notifyPlayNext()
	}

	fun playNext(collection: DomainSongCollection) = this@MediaPlayer.launch {
		val newCollection = collection.sortedSongs()
		if (newCollection.isEmpty()) return@launch
		val items = withContext(Dispatchers.Default) { newCollection.map { it.toMediaItem() } }
		val state = _uiState.value
		val insertAt = (state.currentIndex + 1).coerceIn(0, state.queue.size)
		val newQueue = state.queue.toMutableList().apply { addAll(insertAt, newCollection) }
		val newOrder = insertedShuffleOrder(state, insertAt, newCollection)

		_uiState.update {
			it.copy(
				queue = newQueue,
				shuffleOrder = newOrder,
				currentIndex = if (it.currentIndex == -1) 0 else it.currentIndex,
				currentSong = if (it.currentIndex == -1) newCollection.firstOrNull() else it.currentSong
			)
		}
		controller?.addMediaItems(insertAt, items)
		if (newOrder.isNotEmpty()) pushShuffleOrder(newOrder)
		snackBarManager.notifyPlayNext()
	}

	fun playRadio(radio: DomainRadio) = this@MediaPlayer.launch {
		val radioId = "radio_${radio.name.hashCode()}"

		val dummyRadioSong = DomainSong(
			id = radioId,
			title = radio.name,
			artistName = "Live Radio",
			albumId = "radio_album",
			albumTitle = "Live Stream",
			duration = Duration.ZERO,
			trackNumber = 1,
			coverArtId = radio.coverArtId,
			artistId = "",
			parentId = "",
			comment = null,
			discNumber = null,
			isrc = emptyList(),
			year = null,
			genre = null,
			genres = emptyList(),
			moods = emptyList(),
			bpm = null,
			contributors = emptyList(),
			playCount = 0,
			userRating = 0,
			averageRating = null,
			bitRate = null,
			bitDepth = null,
			sampleRate = null,
			audioChannelCount = null,
			replayGain = null,
			fileSize = 0,
			fileExtension = "",
			mimeType = "",
			filePath = radio.streamUrl,
			starredAt = null,
			musicBrainzId = null,
			explicitStatus = DomainExplicitStatus.Unknown,
			artists = emptyList(),
			albumArtists = emptyList(),
			isExternal = false
		)

		val metadata = MediaMetadata.Builder()
			.setTitle(radio.name)
			.setArtist("Live Radio")
			.setIsPlayable(true)
			.build()

		val mediaItem = MediaItem.Builder()
			.setUri(radio.streamUrl)
			.setMediaId("radio_${radio.name.hashCode()}")
			.setMediaMetadata(metadata)
			.setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build())
			.build()

		controller?.let { player ->
			player.stop()
			player.clearMediaItems()
			player.setMediaItem(mediaItem)
			player.prepare()
			player.play()
		}

		_uiState.update { state ->
			state.copy(
				queue = listOf(dummyRadioSong),
				shuffleOrder = if (state.isShuffleEnabled) listOf(0) else emptyList(),
				currentIndex = 0,
				currentSong = dummyRadioSong,
				isLoading = true
			)
		}
	}

	fun shufflePlay(collection: DomainSongCollection) = this@MediaPlayer.launch {
		val songs = collection.sortedSongs()
		if (songs.isEmpty()) {
			clearQueue()
			return@launch
		}

		val items = withContext(Dispatchers.Default) { songs.map { it.toMediaItem() } }
		val order = songs.indices.shuffled()
		val startIndex = order.first()

		_uiState.update {
			it.copy(
				queue = songs,
				shuffleOrder = order,
				currentIndex = startIndex,
				currentSong = songs[startIndex],
				isShuffleEnabled = true,
				progress = 0f
			)
		}

		controller?.let { player ->
			player.setMediaItems(items, startIndex, 0L)
			pushShuffleOrder(order)
			player.shuffleModeEnabled = true
			player.prepare()
			player.play()
		}
	}

	fun pause() = launch(Dispatchers.Main.immediate) {
		controller?.pause()
	}

	fun resume() = launch(Dispatchers.Main.immediate) {
		controller?.let { player ->
			if (player.playbackState == Player.STATE_ENDED) {
				player.seekTo(uiState.value.shuffleOrder.firstOrNull() ?: 0, 0L)
			}
			player.play()
		}
	}

	fun next() = launch(Dispatchers.Main.immediate) {
		if (controller?.hasNextMediaItem() == true) controller?.seekToNextMediaItem()
	}

	fun previous() = launch(Dispatchers.Main.immediate) {
		val controller = controller ?: return@launch
		if (controller.hasPreviousMediaItem() && controller.currentPosition <= 1000) {
			controller.seekToPreviousMediaItem()
		} else {
			controller.seekTo(0)
		}
	}

	fun toggleShuffle() {
		scope.launch {
			controller?.let { player ->
				player.shuffleModeEnabled = !player.shuffleModeEnabled
			}
		}
	}

	fun toggleRepeat() = launch {
		controller?.let { player ->
			player.repeatMode = when (player.repeatMode) {
				Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
				Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
				else -> Player.REPEAT_MODE_OFF
			}
		}
	}

	fun seek(normalized: Float) = launch(Dispatchers.Main.immediate) {
		controller?.let {
			val target = (it.effectiveDurationMs() * normalized).toLong()
			it.seekTo(target)
			_uiState.update { state ->
				state.copy(progress = normalized)
			}
		}
	}

	fun setPlaybackSpeed(value: Float) = this@MediaPlayer.launch {
		controller?.playbackParameters = PlaybackParameters(
			value,
			if (uiState.value.preservePitch) 1f else value
		)
		_uiState.update { it.copy(playbackSpeed = value) }
	}

	fun setPreservePitch(value: Boolean) = this@MediaPlayer.launch {
		controller?.playbackParameters = PlaybackParameters(
			uiState.value.playbackSpeed,
			if (value) 1f else uiState.value.playbackSpeed
		)
		_uiState.update { it.copy(preservePitch = value) }
	}

	private fun DomainSong.toMediaItem(): MediaItem {
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
}
