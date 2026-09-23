package paige.navic.exoplayer

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ktor.KtorDataSource
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
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import coil3.ImageLoader
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.exoplayer.impl.ExoAudioGainProcessor
import paige.navic.exoplayer.impl.ExoArtworkLoader
import paige.navic.shared.PlaybackService

@OptIn(UnstableApi::class)
class ExoStateHolder(
	private val context: Context,
	sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager,
	private val imageLoader: ImageLoader
) {
	val gainProcessor = ExoAudioGainProcessor()
	private val mutex = Mutex()
	companion object {
		const val COMMAND_SHUFFLE = "COMMAND_SHUFFLE"
		const val COMMAND_REPEAT = "COMMAND_REPEAT"

		fun newSessionToken(context: Context): SessionToken {
			return SessionToken(context, ComponentName(context, PlaybackService::class.java))
		}
	}

	lateinit var playerInstance: ExoPlayer
	lateinit var mediaSession: MediaSession

	init {
		runBlocking {
			createPlayerInstance()
			createMediaSession()
		}
	}

	private class MediaSessionCallback(private val player: ExoPlayer) : MediaSession.Callback {
		override fun onConnect(
			session: MediaSession,
			controller: MediaSession.ControllerInfo
		): MediaSession.ConnectionResult {
			val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
				.buildUpon()
				.add(SessionCommand(COMMAND_SHUFFLE, Bundle.EMPTY))
				.add(SessionCommand(COMMAND_REPEAT, Bundle.EMPTY))
				.build()

			return MediaSession.ConnectionResult.accept(
				sessionCommands,
				MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
			)
		}

		override fun onCustomCommand(
			session: MediaSession,
			controller: MediaSession.ControllerInfo,
			customCommand: SessionCommand,
			args: Bundle
		): ListenableFuture<SessionResult> {
			when (customCommand.customAction) {
				COMMAND_SHUFFLE -> {
					player.shuffleModeEnabled = !player.shuffleModeEnabled
				}

				COMMAND_REPEAT -> {
					player.repeatMode = when (player.repeatMode) {
						Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
						Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
						else -> Player.REPEAT_MODE_OFF
					}
				}
			}

			return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
		}
	}



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

	private val httpDataSourceFactory = KtorDataSource.Factory(sessionManager.api.httpClient)

	private fun createRenderers(context: Context): RenderersFactory {
		return RenderersFactory { handler, _, audioListener, _, _ ->
			arrayOf<BaseRenderer>(
				MediaCodecAudioRenderer(
					context,
					MediaCodecSelector.DEFAULT,
					handler,
					audioListener,
					DefaultAudioSink.Builder(context)
						.setAudioProcessors(arrayOf(gainProcessor))
						.build()
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
				.setDisplayName("Shuffle")
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
				.setDisplayName("Repeat")
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

	suspend fun createPlayerInstance(): ExoPlayer = mutex.withLock {
		val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
		val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)

		playerInstance = ExoPlayer.Builder(
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
			.also {
				it.addListener(object : Player.Listener {
					override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
						mediaSession.setCustomLayout(makeButtons(it))
					}

					override fun onRepeatModeChanged(repeatMode: Int) {
						mediaSession.setCustomLayout(makeButtons(it))
					}
				})
			}

		return playerInstance
	}

	suspend fun createMediaSession(): MediaSession = mutex.withLock {
		val bitmapLoader = ExoArtworkLoader(context, imageLoader)
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

		this.playerInstance.let {
			mediaSession = MediaSession.Builder(context, it)
				.setSessionActivity(sessionPendingIntent)
				.setBitmapLoader(bitmapLoader)
				.setCallback(MediaSessionCallback(it))
				.setCustomLayout(makeButtons(it))
				.build()
		}

		return mediaSession
	}

	fun destroySession() {
		mediaSession.let {
			it.player.stop()
			it.player.release()
			it.release()
		}
	}
}

