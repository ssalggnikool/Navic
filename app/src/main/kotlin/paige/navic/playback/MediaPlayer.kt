/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback

import android.app.Application
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.database.dao.AlbumDao
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
import paige.navic.domain.repository.PlayerStateRepository
import paige.navic.domain.repository.SongRepository
import paige.navic.playback.exoplayer.ExoControllerHandler
import paige.navic.playback.exoplayer.ExoStateHolder
import paige.navic.playback.exoplayer.toMediaItem
import paige.navic.ui.core.PlayerUiState
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration


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

	private var controllerFuture: ListenableFuture<MediaController>? = null
	private var controller: MediaController? = null
	private var controllerHandler: ExoControllerHandler? = null

	private val songsById = mutableMapOf<String, DomainSong>()

	private fun rememberSongs(songs: List<DomainSong>) {
		songs.forEach { songsById[it.id] = it }
	}

	private var pendingSyncState: PlayerUiState? = null

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
		playNowInternal(
			if (collection is DomainAlbum) collection.songs.sortedWith(
				compareBy(
					{ it.discNumber },
					{ it.trackNumber }
				)
			) else collection.songs,
			startIndex
		)
	}

	fun playNow(songs: List<DomainSong>, startIndex: Int = 0) {
		playNowInternal(songs, startIndex)
	}

	private fun playNowInternal(songs: List<DomainSong>, startIndex: Int) =
		this@MediaPlayer.launch {
			val shuffle = uiState.value.isShuffleEnabled
			val index = startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0))
			val items = withContext(Dispatchers.Default) { songs.map { it.toMediaItem() } }
			rememberSongs(songs)

			controllerHandler?.startOrder(songs.size, index)

			controller?.let { player ->
				if (songs.isEmpty()) {
					player.clearMediaItems()
				} else {
					player.setMediaItems(items, index, 0L)
					player.shuffleModeEnabled = shuffle
					controllerHandler?.pushOrder()
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

	fun checkAndAutoFillQueue() {
		if (!preferenceManager.autoFillQueue) return

		val state = uiState.value
		if (state.activeQueue.isEmpty()) return

		val remainingCount = state.activeQueue.size - state.activeIndex

		if (remainingCount <= 1) {
			scope.launch {
				val randomSongs = songRepository.getRandomSongsList(1)
				appendQueue(randomSongs, notify = false)
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
		val savedState = stateRepository.state.first().copy(isPaused = true, isLoading = false)
		rememberSongs(savedState.queue)
		_uiState.value = savedState
		syncPlayerWithState(savedState)
		checkAndAutoFillQueue()
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

	private fun connectService() = launch {
		val sessionToken = ExoStateHolder.newSessionToken(application)
		controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
		controllerFuture?.addListener({
			controller = controllerFuture?.get()
			setupController()
		}, MoreExecutors.directExecutor())
	}

	private fun setupController() = launch {
		controller?.let { controller ->
			controller.addListener(
				ExoControllerHandler(
					context = application,
					controller = controller,
					state = _uiState,
					preferenceManager = preferenceManager,
					downloadManager = downloadManager,
					sessionManager = sessionManager,
					audioGainManager = audioGainManager,
					streamCacheManager = streamCacheManager,
					connectivityManager = connectivityManager,
					songForId = songsById::get,
					rememberSong = { songsById[it.id] = it },
					albumDao = albumDao,
					requestAutoFill = ::checkAndAutoFillQueue
				).also { handler -> controllerHandler = handler }
			)
		}
		downloadManager.allDownloads.first()
		pendingSyncState?.let { state ->
			syncPlayerWithState(state)
			pendingSyncState = null
		}
		checkAndAutoFillQueue()
	}

	fun syncPlayerWithState(state: PlayerUiState) = launch {
		val player = controller

		if (player == null) {
			pendingSyncState = state
			return@launch
		}

		if (state.queue.isEmpty() || player.mediaItemCount > 0) {
			controllerHandler?.updatePlaybackState()
			return@launch
		}

		val mediaItems = withContext(Dispatchers.Default) {
			state.queue.map { it.toMediaItem() }
		}

		val index = if (state.currentIndex in mediaItems.indices) state.currentIndex else 0

		controllerHandler?.startOrder(mediaItems.size, index)

		player.apply {
			setMediaItems(mediaItems)

			shuffleModeEnabled = state.isShuffleEnabled
			repeatMode = state.repeatMode
			playbackParameters = PlaybackParameters(state.playbackSpeed)
		}
		controllerHandler?.pushOrder()

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

	fun addToQueueSingle(song: DomainSong, notify: Boolean = true) = this@MediaPlayer.launch {
		rememberSongs(listOf(song))
		controllerHandler?.appendOrder(1)
		controller?.addMediaItem(song.toMediaItem())
		controllerHandler?.pushOrder()
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	fun appendQueue(collection: DomainSongCollection, notify: Boolean = true) {
		appendQueue(
			if (collection is DomainAlbum) collection.songs.sortedWith(
				compareBy(
					{ it.discNumber },
					{ it.trackNumber }
				)
			) else collection.songs,
			notify
		)
	}

	fun appendQueue(songs: List<DomainSong>, notify: Boolean = true) = this@MediaPlayer.launch {
		val items = songs.map { it.toMediaItem() }
		rememberSongs(songs)
		controllerHandler?.appendOrder(songs.size)
		controller?.addMediaItems(items)
		controllerHandler?.pushOrder()
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	fun removeFromQueue(index: Int) = this@MediaPlayer.launch {
		val normalIndex = uiState.value.normalIndex(index)
		controllerHandler?.removeOrder(index)
		controller?.removeMediaItem(normalIndex)
		controllerHandler?.pushOrder()
	}

	fun moveQueueItem(fromIndex: Int, toIndex: Int) = this@MediaPlayer.launch {
		val state = uiState.value
		if (fromIndex == toIndex ||
			fromIndex !in state.activeQueue.indices ||
			toIndex !in state.activeQueue.indices
		) return@launch

		if (state.isShuffled) {
			controllerHandler?.moveOrder(fromIndex, toIndex)
			controllerHandler?.pushOrder()
		} else {
			controller?.moveMediaItem(fromIndex, toIndex)
			controllerHandler?.clearOrder()
		}
	}

	fun clearQueue() = this@MediaPlayer.launch {
		controllerHandler?.clearOrder()
		controller?.clearMediaItems()
	}

	fun playAt(index: Int) = this@MediaPlayer.launch {
		controller?.let { player ->
			if (index in 0 until player.mediaItemCount) {
				player.seekTo(index, 0L)
				player.play()
			}
		}
	}

	fun playNextSingle(song: DomainSong) = this@MediaPlayer.launch {
		val player = controller ?: return@launch
		val insertAt = player.nextInsertIndex()
		rememberSongs(listOf(song))

		controllerHandler?.insertOrder(insertAt, 1)
		player.addMediaItem(
			insertAt,
			withContext(Dispatchers.Default) { song.toMediaItem() }
		)
		controllerHandler?.pushOrder()
		snackBarManager.notifyPlayNext()
	}

	fun playNext(collection: DomainSongCollection) = this@MediaPlayer.launch {
		val (items, newCollection) = withContext(Dispatchers.Default) {
			val newCollection =
				if (collection is DomainAlbum) collection.songs.sortedWith(
					compareBy(
						{ it.discNumber },
						{ it.trackNumber }
					)) else collection.songs
			newCollection.map { it.toMediaItem() } to newCollection
		}
		val player = controller ?: return@launch
		val insertAt = player.nextInsertIndex()
		rememberSongs(newCollection)

		controllerHandler?.insertOrder(insertAt, items.size)
		player.addMediaItems(insertAt, items)
		controllerHandler?.pushOrder()
		snackBarManager.notifyPlayNext()
	}

	private fun MediaController.nextInsertIndex(): Int {
		val current = currentMediaItemIndex
		if (current == C.INDEX_UNSET) return 0
		val next = currentTimeline.getNextWindowIndex(
			current,
			Player.REPEAT_MODE_OFF,
			shuffleModeEnabled
		)
		return if (next == C.INDEX_UNSET) mediaItemCount else next
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

		rememberSongs(listOf(dummyRadioSong))

		controllerHandler?.startOrder(1, 0)

		controller?.let { player ->
			player.stop()
			player.clearMediaItems()
			player.setMediaItem(mediaItem)
			controllerHandler?.pushOrder()
			player.prepare()
			player.play()
		}
	}

	fun shufflePlay(collection: DomainSongCollection) = this@MediaPlayer.launch {
		val (shuffledSongs, mediaItems) = withContext(Dispatchers.Default) {
			val songs = collection.songs.shuffled()
			songs to songs.map { it.toMediaItem() }
		}

		rememberSongs(shuffledSongs)

		controller?.let { player ->
			player.shuffleModeEnabled = false
			player.setMediaItems(mediaItems, 0, 0L)
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
				player.seekTo(0, 0L)
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
		controllerHandler?.seek(normalized)
	}

	fun setPlaybackSpeed(value: Float) = this@MediaPlayer.launch {
		controllerHandler?.setPlaybackSpeed(value)
	}

	fun setPreservePitch(value: Boolean) = this@MediaPlayer.launch {
		controllerHandler?.setPreservePitch(value)
	}
}
