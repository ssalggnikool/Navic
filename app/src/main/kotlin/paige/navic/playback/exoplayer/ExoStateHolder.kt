/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.playback.exoplayer

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessorChain
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import androidx.media3.exoplayer.BaseRenderer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.flac.FlacExtractor
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp3.Mp3Extractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.extractor.ogg.OggExtractor
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.extractor.ts.AdtsExtractor
import androidx.media3.extractor.wav.WavExtractor
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.R
import paige.navic.domain.manager.playback.EqualizerManager
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.session.connectivity.ReachabilityFlow
import paige.navic.domain.repository.PlayerStateRepository
import paige.navic.playback.exoplayer.impl.BetterShuffleOrder
import paige.navic.playback.exoplayer.impl.cache.NavicDataSource
import paige.navic.playback.exoplayer.impl.library.AndroidAutoLibrary
import paige.navic.playback.exoplayer.impl.library.MediaSessionCallback
import paige.navic.playback.exoplayer.impl.listeners.ExoDiscordIntegration
import paige.navic.playback.exoplayer.impl.listeners.ExoDiscordIntegrationImpl
import paige.navic.playback.exoplayer.impl.listeners.ScrobbleExoListener
import paige.navic.playback.exoplayer.impl.playback.ExoAudioGainProcessor
import paige.navic.playback.exoplayer.impl.playback.ExoEqualizerManager
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(UnstableApi::class)
class ExoStateHolder(
	private val context: Context,
	private val preferenceManager: PreferenceManager,
	private val dataSource: NavicDataSource.NavicDataSourceFactory,
	private val androidAutoLibrary: AndroidAutoLibrary,
	private val stateRepository: PlayerStateRepository,
	private val equalizerManager: EqualizerManager,
	private val reachability: ReachabilityFlow,
	private val syncManager: SyncManager,
	private val sessionManager: SessionManager
) {
	private val mutex = Mutex()
	private val listenerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
	private var equalizerJob: Job? = null
	private var exoEqualizerManager: ExoEqualizerManager? = null
	private var scrobbleListener: ScrobbleExoListener? = null
	private var discordIntegration: ExoDiscordIntegration? = null

	private var isInitialized = false

	// ExoPlaybackService and AudioGainManager depends on these fields below
	lateinit var player: ExoPlayer
	lateinit var mediaSession: MediaLibraryService.MediaLibrarySession

	/** Set by [ExoPlaybackService] so a rebuilt session is unregistered from the service. */
	var onSessionReleased: ((MediaLibraryService.MediaLibrarySession) -> Unit)? = null

	val gainProcessor = ExoAudioGainProcessor()

	private val extractorsFactory = ExtractorsFactory {
		arrayOf(
			FlacExtractor(),
			WavExtractor(),
			FragmentedMp4Extractor(DefaultSubtitleParserFactory.UNSUPPORTED),
			Mp4Extractor(DefaultSubtitleParserFactory.UNSUPPORTED),
			OggExtractor(),
			MatroskaExtractor(DefaultSubtitleParserFactory.UNSUPPORTED),
			AdtsExtractor(AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING),
			Mp3Extractor(Mp3Extractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
		)
	}

	private fun createRenderers(context: Context): RenderersFactory {
		val audioProcessors = arrayOf<AudioProcessor>(gainProcessor)
		val audioSinkBuilder = DefaultAudioSink.Builder(context)

		if (preferenceManager.floatOutputEnabled) {
			// using setAudioProcessors will indirectly use the default audio processor chain
			// which by default uses the silence skipper and playback speed audio processors,
			// they both don't work with anything that isn't 16 bit pcm,
			// so exoplayer (our fork) will just happily ignore the replaygain processor
			// that's why this below is being done
			audioSinkBuilder
				.setEnableFloatOutput(true)
				.setAudioProcessorChain(object : AudioProcessorChain {
					override fun getAudioProcessors(inputFormat: Format): Array<out AudioProcessor> {
						return audioProcessors
					}

					override fun applyPlaybackParameters(playbackParameters: PlaybackParameters): PlaybackParameters {
						return PlaybackParameters.DEFAULT
					}

					override fun applySkipSilenceEnabled(skipSilenceEnabled: Boolean): Boolean {
						return false
					}

					override fun getMediaDuration(playoutDuration: Long): Long {
						return playoutDuration
					}

					override fun getSkippedOutputFrameCount(): Long {
						return 0
					}
				})
		} else {
			audioSinkBuilder.setAudioProcessors(audioProcessors)
		}

		val audioSink = audioSinkBuilder.build()

		return RenderersFactory { eventHandler, _, eventListener, _, _ ->
			arrayOf<BaseRenderer>(
				FfmpegAudioRenderer(
					eventHandler,
					eventListener,
					audioSink
				),
				MediaCodecAudioRenderer(
					context,
					MediaCodecSelector.DEFAULT,
					eventHandler,
					eventListener,
					audioSink
				)
			)
		}
	}

	private fun makeButtons(player: Player) = buildList {
		add(
			CommandButton.Builder(
				if (player.shuffleModeEnabled) {
					CommandButton.ICON_SHUFFLE_ON
				} else {
					CommandButton.ICON_SHUFFLE_OFF
				}
			)
				.setDisplayName(context.getString(R.string.action_shuffle))
				.setSessionCommand(SessionCommand(COMMAND_SHUFFLE, Bundle.EMPTY))
				.build()
		)

		add(
			CommandButton.Builder(
				when (player.repeatMode) {
					Player.REPEAT_MODE_OFF -> CommandButton.ICON_REPEAT_OFF
					Player.REPEAT_MODE_ALL -> CommandButton.ICON_REPEAT_ALL
					else -> CommandButton.ICON_REPEAT_ONE
				}
			)
				.setDisplayName(context.getString(R.string.action_repeat))
				.setSessionCommand(SessionCommand(COMMAND_REPEAT, Bundle.EMPTY))
				.build()
		)
	}

	private val loadControl: LoadControl = DefaultLoadControl.Builder().apply {
		setBufferDurationsMs(
			/* minBufferMs = */ 32_000,
			/* maxBufferMs = */ 64_000,
			/* bufferForPlaybackMs = */ 2_500,
			/* bufferForPlaybackAfterRebufferMs = */ 5_000
		)
		setBackBuffer(10_000, true)
	}.build()

	private suspend fun createPlayerInstance(): ExoPlayer = mutex.withLock {
		val mediaSourceFactory = DefaultMediaSourceFactory(dataSource, extractorsFactory)

		player = ExoPlayer.Builder(
			context,
			createRenderers(context)
		).apply {
			setLoadControl(loadControl)
			setMediaSourceFactory(mediaSourceFactory)
			setHandleAudioBecomingNoisy(true)
			setWakeMode(C.WAKE_MODE_NETWORK)
		}.build()
			.apply {
				setAudioAttributes(
					AudioAttributes.Builder()
						.setUsage(C.USAGE_MEDIA)
						.setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
						.build(),
					true
				)

				trackSelectionParameters =
					trackSelectionParameters.buildUpon().setAudioOffloadPreferences(
						TrackSelectionParameters.AudioOffloadPreferences
							.Builder()
							.setIsGaplessSupportRequired(preferenceManager.gaplessPlayback)
							.setAudioOffloadMode(
								if (preferenceManager.audioOffload) {
									TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
								} else {
									TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
								}
							)
							.build()
					).build()
			}
			.also { player ->
				player.preloadConfiguration = ExoPlayer.PreloadConfiguration(
					10.seconds.toLong(DurationUnit.MICROSECONDS)
				)

				player.addListener(object : Player.Listener {
					override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
						mediaSession.setCustomLayout(makeButtons(player))
					}

					override fun onRepeatModeChanged(repeatMode: Int) {
						mediaSession.setCustomLayout(makeButtons(player))
					}
				})

				player.addListener(
					ExoDiscordIntegrationImpl(player).also { discordIntegration = it }
				)

				attachPlayerListeners(player)
			}

		return player
	}

	private fun attachPlayerListeners(player: ExoPlayer) {
		scrobbleListener = ScrobbleExoListener(
			player,
			reachability,
			syncManager,
			sessionManager,
			preferenceManager
		)

		exoEqualizerManager = ExoEqualizerManager(equalizerManager, context).apply {
			player.addListener(this)
			applyEqualizerMode(equalizerManager.config.value.mode)
			equalizerJob = listenerScope.launch {
				equalizerManager.config.collect { updateConfig() }
			}
		}
	}

	private suspend fun createMediaSession(): MediaLibraryService.MediaLibrarySession =
		mutex.withLock {
			val sessionIntent = context.packageManager
				.getLaunchIntentForPackage(context.packageName)
				?.apply {
					flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
						Intent.FLAG_ACTIVITY_CLEAR_TOP
				}

			val sessionPendingIntent = PendingIntent.getActivity(
				context,
				0,
				sessionIntent,
				PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
			)

			this.player.let {
				mediaSession = MediaLibraryService.MediaLibrarySession.Builder(
					context,
					it,
					MediaSessionCallback(it, androidAutoLibrary)
				)
					.setPeriodicPositionUpdateEnabled(false) // fix android auto queue randomly jumping to the top
					.setSessionActivity(sessionPendingIntent)
					.setCustomLayout(makeButtons(it))
					.build()
			}

			return mediaSession
		}

	fun initState() = runBlocking {
		if (isInitialized) return@runBlocking

		createPlayerInstance()
		createMediaSession()

		// the service can start without the UI, so restore the last mode here too
		val savedState = stateRepository.state.first()
		player.shuffleModeEnabled = savedState.isShuffleEnabled
		player.repeatMode = savedState.repeatMode
		// auto builds the queue before any order is pushed, so seed ours to keep the chosen song first
		player.shuffleOrder = BetterShuffleOrder(
			player.mediaItemCount,
			player.currentMediaItemIndex
		)

		isInitialized = !isInitialized
	}

	fun destroySession() {
		if (!isInitialized) return

		equalizerJob?.cancel()
		equalizerJob = null
		exoEqualizerManager?.releaseEqualizer()
		exoEqualizerManager = null
		scrobbleListener?.release()
		scrobbleListener = null
		discordIntegration?.release()
		discordIntegration = null

		onSessionReleased?.invoke(mediaSession)

		mediaSession.let {
			it.player.stop()
			it.player.release()
			it.release()
		}
		isInitialized = !isInitialized
	}

	fun restartPlayback() {
		if (!isInitialized) return
		destroySession()
		initState()
	}

	fun reloadNetworkDataSource() {
		if (!isInitialized) return

		val player = player
		val mediaItemIndex = player.currentMediaItemIndex
		val mediaItem = player.currentMediaItem ?: return
		if (mediaItemIndex == C.INDEX_UNSET) return

		val position = player.currentPosition.coerceAtLeast(0L)
		val playWhenReady = player.playWhenReady
		player.replaceMediaItem(mediaItemIndex, mediaItem)
		player.seekTo(mediaItemIndex, position)
		player.playWhenReady = playWhenReady
	}

	companion object {
		const val COMMAND_SHUFFLE = "COMMAND_SHUFFLE"
		const val COMMAND_REPEAT = "COMMAND_REPEAT"


		fun newSessionToken(context: Context): SessionToken {
			return SessionToken(context, ComponentName(context, ExoPlaybackService::class.java))
		}
	}
}
