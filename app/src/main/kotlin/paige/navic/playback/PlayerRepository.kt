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
import paige.navic.R
import paige.navic.database.dao.AlbumDao
import paige.navic.domain.manager.playback.AudioGainManager
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
import paige.navic.playback.exoplayer.impl.cache.StreamCacheManager
import paige.navic.playback.exoplayer.toMediaItem
import paige.navic.ui.core.PlayerUiState
import paige.navic.ui.core.QueueSong
import paige.navic.ui.core.toQueueSong
import paige.navic.util.Logger
import java.util.WeakHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration


@androidx.annotation.OptIn(UnstableApi::class)
class PlayerRepository(
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

	private val queueSongsById = WeakHashMap<String, QueueSong>()
	private val currentSongsById = WeakHashMap<String, DomainSong>()

	private fun rememberSongs(songs: List<DomainSong>) {
		songs.forEach { queueSongsById[it.id] = it.toQueueSong() }
	}

	private fun rememberCurrentSong(song: DomainSong) {
		currentSongsById.clear()
		currentSongsById[song.id] = song
		queueSongsById[song.id] = song.toQueueSong()
	}

	private fun retainQueueSongs(ids: Collection<String>) {
		queueSongsById.keys.retainAll(ids.toSet())
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

	init {
		connectService()
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
		launch {
			val index = startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0))
			val items = withContext(Dispatchers.Default) { songs.map { it.toMediaItem() } }
			rememberSongs(songs)
			songs.getOrNull(index)?.let(::rememberCurrentSong)
			if (songs.isEmpty()) {
				queueSongsById.clear()
				currentSongsById.clear()
			}

			controllerHandler?.startOrder(songs.size, index)

			controller?.let { player ->
				if (songs.isEmpty()) {
					player.clearMediaItems()
				} else {
					player.setMediaItems(items, index, 0L)
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
			launch {
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

	suspend fun retrievePersistedState(): PlayerUiState {
		val savedState = stateRepository.state.first().copy(isPaused = true, isLoading = false)
		savedState.queue.forEach { queueSongsById[it.id] = it }
		savedState.currentSong?.let(::rememberCurrentSong)
		_uiState.value = savedState
		return savedState
	}

	suspend fun restoreState(controller: MediaController) {
		val savedState = retrievePersistedState()

		if (savedState.queue.isEmpty() || controller.mediaItemCount > 0) {
			controllerHandler?.updatePlaybackState()
			return
		}

		val mediaItems = withContext(Dispatchers.Default) {
			savedState.queue.map { it.toMediaItem() }
		}

		val index =
			if (savedState.currentIndex in mediaItems.indices) savedState.currentIndex else 0

		controllerHandler?.startOrder(mediaItems.size, index)

		controller.apply {
			setMediaItems(mediaItems)

			this.shuffleModeEnabled = savedState.isShuffleEnabled
			this.repeatMode = savedState.repeatMode
			this.playbackParameters = PlaybackParameters(savedState.playbackSpeed)
		}
		controllerHandler?.pushOrder()

		val songDurationMs =
			savedState.queue.getOrNull(index)?.duration?.inWholeMilliseconds ?: 0L

		val position = if (songDurationMs > 0) {
			(savedState.progress * songDurationMs).toLong()
		} else {
			0L
		}

		controller.seekTo(index, position)
		controller.prepare()
		if (!savedState.isPaused) {
			controller.play()
		}
		checkAndAutoFillQueue()
	}

	private fun observeAndSaveState() = launch {
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
					songForId = currentSongsById::get,
					queueSongForId = queueSongsById::get,
					rememberSong = { rememberCurrentSong(it) },
					rememberQueueSongs = ::rememberSongs,
					retainQueueSongs = ::retainQueueSongs,
					albumDao = albumDao,
					songRepository = songRepository,
					requestAutoFill = ::checkAndAutoFillQueue
				).also {
					handler -> controllerHandler = handler
					restoreState(controller)
					observeAndSaveState()
				}
			)
			downloadManager.allDownloads.first()
		}
	}

	fun addToQueueSingle(song: DomainSong, notify: Boolean = true) {
		rememberSongs(listOf(song))
		if (uiState.value.queue.isEmpty()) rememberCurrentSong(song)
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

	fun appendQueue(songs: List<DomainSong>, notify: Boolean = true) {
		val items = songs.map { it.toMediaItem() }
		rememberSongs(songs)
		if (uiState.value.queue.isEmpty()) songs.firstOrNull()?.let(::rememberCurrentSong)
		controllerHandler?.appendOrder(songs.size)
		controller?.addMediaItems(items)
		controllerHandler?.pushOrder()
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	fun removeFromQueue(index: Int) {
		val normalIndex = uiState.value.normalIndex(index)
		controllerHandler?.removeOrder(index)
		controller?.removeMediaItem(normalIndex)
		controllerHandler?.pushOrder()
	}

	fun moveQueueItem(fromIndex: Int, toIndex: Int) {
		val state = uiState.value
		if (fromIndex == toIndex ||
			fromIndex !in state.activeQueue.indices ||
			toIndex !in state.activeQueue.indices
		) return

		if (state.isShuffled) {
			controllerHandler?.moveOrder(fromIndex, toIndex)
			controllerHandler?.pushOrder()
		} else {
			controller?.moveMediaItem(fromIndex, toIndex)
			controllerHandler?.clearOrder()
		}
	}

	fun clearQueue() {
		controllerHandler?.clearOrder()
		controller?.clearMediaItems()
		queueSongsById.clear()
		currentSongsById.clear()
	}

	fun playAt(index: Int) {
		val controller = controller ?: return

		if (index in 0 until controller.mediaItemCount) {
			controller.seekTo(index, 0L)
			controller.play()
		}
	}

	fun playNextSingle(song: DomainSong) = launch {
		val player = controller ?: return@launch
		val item = withContext(Dispatchers.Default) { song.toMediaItem() }

		val insertAt = player.nextInsertIndex()
		rememberSongs(listOf(song))

		controllerHandler?.insertOrder(insertAt, 1)
		player.addMediaItem(insertAt, item)
		controllerHandler?.pushOrder()
		snackBarManager.notifyPlayNext()
	}

	fun playNext(collection: DomainSongCollection) = launch {
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

	fun playInstantMix(seed: DomainSong) = this@PlayerRepository.launch {
		val similar = songRepository.getSimilarSongs(seed.id).getOrElse {
			Logger.e("MediaPlayer", "Failed to fetch similar songs: ${it.message}")
			snackBarManager.notify(R.string.notice_instant_mix_failed)
			return@launch
		}
		val mix = similar.filter { it.id != seed.id }
		if (mix.isEmpty()) {
			snackBarManager.notify(R.string.notice_instant_mix_empty)
			return@launch
		}
		playNowInternal(listOf(seed) + mix, 0)
	}

	fun playRadio(radio: DomainRadio) = launch {
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
		rememberCurrentSong(dummyRadioSong)

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

	fun pause() = launch(Dispatchers.Main.immediate) {
		controller?.pause()
	}

	fun resume() = launch(Dispatchers.Main.immediate) {
		val controller = controller ?: return@launch

		if (controller.playbackState == Player.STATE_ENDED) {
			controller.seekTo(0, 0L)
		}
		controller.play()
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
		val controller = controller ?: return
		controllerHandler?.startOrder(controller.mediaItemCount, controller.currentMediaItemIndex)
		controller.shuffleModeEnabled = !controller.shuffleModeEnabled
	}

	fun setShuffle(mode: Boolean) {
		val controller = controller ?: return
		controllerHandler?.startOrder(controller.mediaItemCount, controller.currentMediaItemIndex)
		controller.shuffleModeEnabled = mode
	}

	fun toggleRepeat() {
		val controller = controller ?: return

		controller.repeatMode = when (controller.repeatMode) {
			Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
			Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
			else -> Player.REPEAT_MODE_OFF
		}
	}

	fun seek(normalized: Float) = launch(Dispatchers.Main.immediate) {
		controllerHandler?.seek(normalized)
	}

	fun setPlaybackSpeed(value: Float) = launch {
		controllerHandler?.setPlaybackSpeed(value)
	}

	fun setPreservePitch(value: Boolean) = launch {
		controllerHandler?.setPreservePitch(value)
	}
}
