package paige.navic.shared

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.di.ResourceProvider
import paige.navic.domain.manager.AndroidScrobbleManager
import paige.navic.domain.manager.AudioGainManager
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.EqualiserManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.domain.repositories.SongRepository
import paige.navic.exoplayer.ExoStateHolder
import paige.navic.exoplayer.listeners.ExoEqualizerManager
import paige.navic.ui.core.PlayerUiState
import paige.navic.util.Logger
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService(), KoinComponent {
	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	private val serviceScope = MainScope()

	private val stateHolder: ExoStateHolder by inject()
	private val resourceProvider: ResourceProvider by inject()

	private val connectivityManager: ConnectivityManager by inject()

	private val syncManager: SyncManager by inject()
	private val sessionManager: SessionManager by inject()
	private val preferenceManager: PreferenceManager by inject()
	private val equaliserManager: EqualiserManager by inject()
	private val exoEqualizerManager: ExoEqualizerManager by inject()

	private lateinit var scrobbleManager: AndroidScrobbleManager

	override fun onCreate() {
		super.onCreate()

		val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
			.build().apply {
				setSmallIcon(resourceProvider.icNavic)
			}

		setMediaNotificationProvider(notificationProvider)

		stateHolder.playerInstance.let {
			it as Player

			scrobbleManager = AndroidScrobbleManager(
				it,
				serviceScope,
				connectivityManager,
				syncManager,
				sessionManager,
				preferenceManager
			)

			it.addListener(exoEqualizerManager)

			exoEqualizerManager.apply {
				applyEqualiserMode(equaliserManager.config.value.mode)
				scope.launch(Dispatchers.Main) {
					equaliserManager.config.collect { updateEqualiser() }
				}
			}
		}
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return stateHolder.mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		pauseAllPlayersAndStopSelf()
	}

	override fun onDestroy() {
		scrobbleManager.release()
		serviceScope.cancel()
		stopForeground(STOP_FOREGROUND_REMOVE)
		stateHolder.destroySession()
		super.onDestroy()
		stopSelf()
	}
}

@UnstableApi
class AndroidMediaPlayerViewModel(
	stateRepository: PlayerStateRepository,
	songRepository: SongRepository,
	downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager,
	preferenceManager: PreferenceManager,
	private val audioGainManager: AudioGainManager,
	private val application: Application,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : MediaPlayerViewModel(
	stateRepository = stateRepository,
	songRepository = songRepository,
	connectivityManager = connectivityManager,
	downloadManager = downloadManager,
	preferenceManager = preferenceManager
) {
	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null

	private var loadingCollectionId: String? = null

	private var pendingSyncState: PlayerUiState? = null

	init {
		connectToService()
	}

	// just because i hate writing boilerplate every single line
	private fun launchInView(immediate: Boolean = false, block: suspend CoroutineScope.() -> Unit) {
		val context = if (immediate) {
			Dispatchers.Main.immediate
		} else {
			EmptyCoroutineContext
		}

		viewModelScope.launch(
			context = context,
			block = block
		)
	}

	private fun connectToService() = launchInView {
		val sessionToken = ExoStateHolder.newSessionToken(application)
		controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
		controllerFuture?.addListener({
			controller = controllerFuture?.get()
			setupController()
		}, MoreExecutors.directExecutor())
	}

	// TODO: I THINK this could be somewhere else
	private fun getStreamUrl(id: String): Uri {
		val isCellular = connectivityManager.isCellular.value
		val bitrate = if (preferenceManager.isAdvancedTranscodingActive) {
			if (isCellular) preferenceManager.customMaxBitrateCellular else preferenceManager.customMaxBitrateWifi
		} else {
			if (isCellular) preferenceManager.streamingQualityCellular.bitrateAndroid else preferenceManager.streamingQualityWifi.bitrateAndroid
		}
		val container = if (preferenceManager.isAdvancedTranscodingActive) {
			if (isCellular) preferenceManager.customFormatCellular else preferenceManager.customFormatWifi
		} else {
			if (isCellular) preferenceManager.streamingQualityCellular.containerAndroid else preferenceManager.streamingQualityWifi.containerAndroid
		}
		return sessionManager.api.getStreamUrl(id, bitrate, container?.takeIf { it.isNotBlank() })
			.toUri()
			.buildUpon()
			.appendQueryParameter("estimateContentLength", "true")
			.build()
	}

	private fun setupController() = launchInView {
		controller?.apply {
			addListener(object : Player.Listener {
				override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
					updatePlaybackState()
					skipUnavailableSong()
					checkAndAutoFillQueue()
				}

				override fun onIsPlayingChanged(isPlaying: Boolean) {
					if (isPlaying) startProgressLoop()

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
								currentSong?.coverArtId?.let {
									sessionManager.getCoverArtUrl(it)
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
					_uiState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
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
			})
			updatePlaybackState()
			updatePlaybackProperties(currentTracks)

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
		val currentSong = _uiState.value.currentSong ?: return
		if (!isExplicit(currentSong)) return
		Logger.i("MediaPlayer", "trying to skip unavailable song")
		val queue = _uiState.value.queue
		val currentIdx = queue.indexOf(currentSong)

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

		if (nextAvailableIdx <= currentIdx) {
			Logger.i(
				"MediaPlayer",
				"skipping and pausing because the last song in the queue was unavailable"
			)
			controller?.seekTo(nextAvailableIdx, 0L)
			controller?.pause()
		} else {
			// just skip to the next song
			controller?.seekTo(nextAvailableIdx, 0L)
		}
	}

	private fun refreshCurrentCollection(albumId: String) {
		if (loadingCollectionId == albumId) return
		loadingCollectionId = albumId

		viewModelScope.launch {
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
				isPaused = !controller.playWhenReady,
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

	override fun syncPlayerWithState(state: PlayerUiState) = launchInView {
		val player = controller

		if (player == null) {
			pendingSyncState = state
			return@launchInView
		}

		if (state.queue.isEmpty() || player.mediaItemCount > 0) {
			updatePlaybackState()
			return@launchInView
		}

		val mediaItems = withContext(Dispatchers.Default) {
			state.queue.map { it.toMediaItem() }
		}

		player.apply {
			setMediaItems(mediaItems)

			shuffleModeEnabled = state.isShuffleEnabled
			repeatMode = state.repeatMode
			playbackParameters = PlaybackParameters(state.playbackSpeed)
		}

		val index = if (state.currentIndex in mediaItems.indices) state.currentIndex else 0

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

	private fun startProgressLoop() = launchInView {
		while (controller?.isPlaying == true) {
			val player = controller ?: break
			val duration = player.duration
			if (duration > 0) {
				val progress =
					(player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
				_uiState.update { it.copy(progress = progress) }
			}
			delay(200.milliseconds)
		}
	}

	private fun updateProgress() = controller?.also { player ->
		val duration = player.duration
		if (duration > 0) {
			val pos = player.currentPosition
			val progress = (pos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
			_uiState.update { it.copy(progress = progress) }
		}
	}

	@OptIn(UnstableApi::class)
	private fun updatePlaybackProperties(tracks: Tracks) {
		val audioGroup =
			tracks.groups.firstOrNull { it.type == C.TRACK_TYPE_AUDIO && it.isSelected }
		if (audioGroup != null) {
			for (i in 0 until audioGroup.length) {
				if (audioGroup.isTrackSelected(i)) {
					val format = audioGroup.getTrackFormat(i)
					Logger.i("MediaPlayer", "active track format: $format")
					_uiState.update { state ->
						state.copy(
							playbackBitrate = format.bitrate.takeIf { it > 0 },
							playbackSampleRate = format.sampleRate.takeIf { it > 0 },
							playbackMimeType = format.sampleMimeType
						)
					}
					break
				}
			}
		}
	}

	override fun addToQueueSingle(song: DomainSong, notify: Boolean) = launchInView {
		controller?.addMediaItem(song.toMediaItem())
		_uiState.update { state ->
			val newQueue = state.queue + song
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) song else state.currentSong
			)
		}
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	override fun addToQueue(collection: DomainSongCollection, notify: Boolean) {
		addToQueue(
			if (collection is DomainAlbum) collection.songs.sortedWith(
				compareBy(
					{ it.discNumber },
					{ it.trackNumber }
				)
			) else collection.songs,
			notify
		)
	}

	override fun addToQueue(songs: List<DomainSong>, notify: Boolean) = launchInView {
		val items = songs.map { it.toMediaItem() }
		controller?.addMediaItems(items)
		_uiState.update { state ->
			val newQueue = state.queue + songs
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) songs.firstOrNull() else state.currentSong
			)
		}
		if (notify) snackBarManager.notifyAddedToQueue()
	}

	override fun removeFromQueue(index: Int) = launchInView {
		controller?.removeMediaItem(index)
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply { removeAt(index) }
			val newIndex = when {
				index < state.currentIndex -> state.currentIndex - 1
				index == state.currentIndex -> if (newQueue.isEmpty()) -1 else state.currentIndex.coerceAtMost(
					newQueue.size - 1
				)

				else -> state.currentIndex
			}
			state.copy(
				queue = newQueue,
				currentIndex = newIndex,
				currentSong = if (newIndex == -1) null else newQueue[newIndex]
			)
		}
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) = launchInView {
		controller?.moveMediaItem(fromIndex, toIndex)
		_uiState.update { state ->
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
			state.copy(
				queue = newQueue,
				currentIndex = newIndex,
				currentSong = if (newIndex == -1) null else newQueue[newIndex]
			)
		}
	}

	override fun clearQueue() = launchInView {
		_uiState.update {
			it.copy(
				queue = emptyList(),
				currentSong = null,
				currentIndex = -1,
				progress = 0f
			)
		}
		controller?.clearMediaItems()
	}

	override fun playAt(index: Int) = launchInView {
		controller?.let { player ->
			if (index in 0 until player.mediaItemCount) {
				player.seekTo(index, 0L)
				player.play()
			}
		}
	}

	override fun playNextSingle(song: DomainSong) = launchInView {
		controller?.addMediaItem(
			_uiState.value.currentIndex + 1,
			withContext(Dispatchers.Default) { song.toMediaItem() }
		)
		_uiState.update { state ->
			val newQueue =
				if (state.queue.isEmpty())
					state.queue + song
				else
					state.queue.slice(0..state.currentIndex) + song + state.queue.slice(state.currentIndex + 1..<state.queue.size)
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) song else state.currentSong
			)
		}
		snackBarManager.notifyPlayNext()
	}

	override fun playNext(collection: DomainSongCollection) = launchInView {
		val (items, newCollection) = withContext(Dispatchers.Default) {
			val newCollection =
				if (collection is DomainAlbum) collection.songs.sortedWith(
					compareBy(
						{ it.discNumber },
						{ it.trackNumber }
					)) else collection.songs
			newCollection.map { it.toMediaItem() } to newCollection
		}
		controller?.addMediaItems(_uiState.value.currentIndex + 1, items)
		_uiState.update { state ->
			val newQueue =
				if (state.queue.isEmpty())
					state.queue + newCollection
				else
					state.queue.slice(0..state.currentIndex) + newCollection + state.queue.slice(
						state.currentIndex + 1..<state.queue.size
					)
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) newCollection.firstOrNull() else state.currentSong
			)
		}
		snackBarManager.notifyPlayNext()
	}

	override fun playRadio(radio: DomainRadio) = launchInView {
		val radioId = "radio_${radio.name.hashCode()}"

		val dummyRadioSong = DomainSong(
			id = radioId,
			title = radio.name,
			artistName = "Live Radio",
			albumId = "radio_album",
			albumTitle = "Live Stream",
			duration = Duration.ZERO,
			trackNumber = 1,
			coverArtId = null,
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
				currentIndex = 0,
				currentSong = dummyRadioSong,
				isLoading = true
			)
		}
	}

	override fun shufflePlay(collection: DomainSongCollection) = launchInView {
		val (shuffledSongs, mediaItems) = withContext(Dispatchers.Default) {
			val songs = collection.songs.shuffled()
			songs to songs.map { it.toMediaItem() }
		}

		controller?.let { player ->
			player.shuffleModeEnabled = false
			player.setMediaItems(mediaItems, 0, 0L)
			player.prepare()
			player.play()
		}

		_uiState.update { state ->
			state.copy(
				queue = shuffledSongs,
				currentIndex = 0,
				currentSong = shuffledSongs.firstOrNull()
			)
		}
	}

	override fun pause() = launchInView(true) {
		controller?.pause()
	}

	override fun resume() = launchInView(true) {
		controller?.play()
	}

	override fun next() = launchInView(true) {
		if (controller?.hasNextMediaItem() == true) controller?.seekToNextMediaItem()
	}

	override fun previous() = launchInView(true) {
		val controller = controller ?: return@launchInView
		if (controller.hasPreviousMediaItem() && controller.currentPosition <= 1000) {
			controller.seekToPreviousMediaItem()
		} else {
			controller.seekTo(0)
		}
	}

	override fun toggleShuffle() {
		viewModelScope.launch {
			controller?.let { player ->
				player.shuffleModeEnabled = !player.shuffleModeEnabled
			}
		}
	}

	override fun toggleRepeat() = launchInView {
		controller?.let { player ->
			player.repeatMode = when (player.repeatMode) {
				Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
				Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
				else -> Player.REPEAT_MODE_OFF
			}
		}
	}

	override fun seek(normalized: Float) = launchInView(true) {
		controller?.let {
			val target = (it.duration * normalized).toLong()
			it.seekTo(target)
			_uiState.update { state ->
				state.copy(progress = normalized)
			}
		}

	}

	override fun onCleared() = launchInView {
		super.onCleared()
		controllerFuture?.let { MediaController.releaseFuture(it) }
	}

	override fun setPlaybackSpeed(value: Float) = launchInView {
		controller?.setPlaybackSpeed(value)
		_uiState.update { it.copy(playbackSpeed = value) }
	}

	private fun DomainSong.toMediaItem(): MediaItem {
		val displayArtist = artists.joinToString { it.name }.ifBlank { artistName }
		val albumArtistName = albumArtists.joinToString { it.name }.ifBlank { artistName }

		val metadataBuilder = MediaMetadata.Builder()
			.setTitle(title)
			.setSubtitle(displayArtist)
			.setArtist(displayArtist)
			.setAlbumArtist(albumArtistName)
			.setAlbumTitle(albumTitle)
			.setDurationMs(duration.inWholeMilliseconds)
			.setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)

		metadataBuilder.setArtworkUri(
			coverArtId?.let { sessionManager.getCoverArtUrl(it).toUri() }
		)

		val metadata = metadataBuilder.build()

		val uri = when {
			id.startsWith("radio_") && !filePath.isNullOrEmpty() -> {
				filePath.toUri()
			}

			else -> {
				val localPath = downloadManager.getDownloadedFilePath(id)
				if (localPath != null) {
					File(localPath).toUri()
				} else {
					getStreamUrl(id)
				}
			}
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
