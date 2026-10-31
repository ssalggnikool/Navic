package paige.navic.playback.exoplayer.impl.library

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import paige.navic.playback.exoplayer.ExoStateHolder.Companion.COMMAND_REPEAT
import paige.navic.playback.exoplayer.ExoStateHolder.Companion.COMMAND_SHUFFLE
import paige.navic.playback.exoplayer.impl.BetterShuffleOrder
import kotlinx.coroutines.guava.future

@OptIn(UnstableApi::class)
internal class MediaSessionCallback(
	private val player: ExoPlayer,
	private val library: AndroidAutoLibrary,
) : MediaLibraryService.MediaLibrarySession.Callback {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	companion object {
		// commands
		const val COMMAND_SHUFFLE_STATE = "COMMAND_SHUFFLE_STATE"

		// keys
		const val SHUFFLE_ORDER_KEY = "shuffle_order"
		const val SHUFFLE_ENABLED_KEY = "shuffle_enabled"
	}

	override fun onConnect(
		session: MediaSession,
		controller: MediaSession.ControllerInfo
	): MediaSession.ConnectionResult {
		val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
			.buildUpon()
			.apply {
				add(SessionCommand(COMMAND_SHUFFLE, Bundle.EMPTY))
				add(SessionCommand(COMMAND_SHUFFLE_STATE, Bundle.EMPTY))
				add(SessionCommand(COMMAND_REPEAT, Bundle.EMPTY))
			}
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
		val resultExtras = Bundle()

		when (customCommand.customAction) {
			COMMAND_SHUFFLE -> {
				val enabled = args.getBoolean(
					SHUFFLE_ENABLED_KEY, !player.shuffleModeEnabled
				)

				args.getIntArray(SHUFFLE_ORDER_KEY)
					?.takeIf { it.size == player.mediaItemCount }
					?.let { player.shuffleOrder = BetterShuffleOrder(it) }

				player.shuffleModeEnabled = enabled

				session.broadcastCustomCommand(
					SessionCommand(COMMAND_SHUFFLE_STATE, Bundle.EMPTY),
					Bundle().apply {
						putBoolean(SHUFFLE_ENABLED_KEY, enabled)
						putIntArray(SHUFFLE_ORDER_KEY, player.shuffleOrder.toIntArray())
					}
				)
			}

			COMMAND_REPEAT -> {
				player.repeatMode = when (player.repeatMode) {
					Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
					Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
					else -> Player.REPEAT_MODE_OFF
				}
			}
		}

		return Futures.immediateFuture(
			SessionResult(
				SessionResult.RESULT_SUCCESS,
				resultExtras
			)
		)
	}

	override fun onGetLibraryRoot(
		session: MediaLibraryService.MediaLibrarySession,
		browser: MediaSession.ControllerInfo,
		params: MediaLibraryService.LibraryParams?
	): ListenableFuture<LibraryResult<MediaItem>> =
		scope.future {
			LibraryResult.ofItem(library.getItem(AndroidAutoLibrary.ROOT_ID)!!, params)
		}

	override fun onGetItem(
		session: MediaLibraryService.MediaLibrarySession,
		browser: MediaSession.ControllerInfo,
		mediaId: String
	): ListenableFuture<LibraryResult<MediaItem>> = scope.future {
		library.getItem(mediaId)?.let { LibraryResult.ofItem(it, null) }
			?: LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
	}

	override fun onGetChildren(
		session: MediaLibraryService.MediaLibrarySession,
		browser: MediaSession.ControllerInfo,
		parentId: String,
		page: Int,
		pageSize: Int,
		params: MediaLibraryService.LibraryParams?
	): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
		scope.future {
			LibraryResult.ofItemList(
				library.getChildren(parentId, page, pageSize),
				params
			)
		}

	override fun onAddMediaItems(
		mediaSession: MediaSession,
		controller: MediaSession.ControllerInfo,
		mediaItems: List<MediaItem>
	): ListenableFuture<List<MediaItem>> {
		return if (controller.isAndroidAuto()) {
			scope.future {
				mediaItems.firstOrNull()
					?.let { library.expand(it.mediaId) }
					.orEmpty()
			}
		} else {
			super.onAddMediaItems(mediaSession, controller, mediaItems)
		}
	}

	override fun onSetMediaItems(
		mediaSession: MediaSession,
		controller: MediaSession.ControllerInfo,
		mediaItems: List<MediaItem>,
		startIndex: Int,
		startPositionMs: Long
	): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
		if (!controller.isAndroidAuto() || mediaItems.size != 1) {
			return super.onSetMediaItems(
				mediaSession,
				controller,
				mediaItems,
				startIndex,
				startPositionMs
			)
		}

		return scope.future {
			val requestedItem = mediaItems.first()
			val expandedItems = library.expand(requestedItem.mediaId)
			val requestedSongId = requestedItem.mediaId.substringAfterLast(":")
			val expandedIndex = expandedItems.indexOfFirst {
				it.mediaId == requestedSongId
			}

			MediaSession.MediaItemsWithStartPosition(
				expandedItems,
				expandedIndex.takeIf { it >= 0 } ?: startIndex,
				startPositionMs
			)
		}
	}

	override fun onSearch(
		session: MediaLibraryService.MediaLibrarySession,
		browser: MediaSession.ControllerInfo,
		query: String,
		params: MediaLibraryService.LibraryParams?
	): ListenableFuture<LibraryResult<Void>> {
		val count = library.searchCount(query)
		session.notifySearchResultChanged(browser, query, count, params)
		return Futures.immediateFuture(LibraryResult.ofVoid())
	}

	override fun onGetSearchResult(
		session: MediaLibraryService.MediaLibrarySession,
		browser: MediaSession.ControllerInfo,
		query: String,
		page: Int,
		pageSize: Int,
		params: MediaLibraryService.LibraryParams?
	): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = scope.future {
		LibraryResult.ofItemList(
			library.search(query, page, pageSize),
			params
		)
	}

	private fun MediaSession.ControllerInfo.isAndroidAuto(): Boolean =
		packageName == "com.google.android.projection.gearhead" ||
			packageName == "com.google.android.car.media"

	private fun androidx.media3.exoplayer.source.ShuffleOrder.toIntArray(): IntArray {
		val order = IntArray(length)
		var index = firstIndex
		for (position in order.indices) {
			order[position] = index
			index = getNextIndex(index)
		}
		return order
	}
}
