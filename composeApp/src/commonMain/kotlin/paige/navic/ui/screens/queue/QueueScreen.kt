package paige.navic.ui.screens.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_clear_queue
import navic.composeapp.generated.resources.count_remaining_songs
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.info_duration_left
import navic.composeapp.generated.resources.info_no_queue
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSheetState
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.QueueInfoType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.queue.components.QueueScreenItem
import paige.navic.ui.screens.queue.viewmodels.QueueViewModel
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.util.draggableItemsIndexed
import paige.navic.ui.util.rememberDraggableListState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen() {
	val viewModel = koinViewModel<QueueViewModel>()
	val backStack = LocalNavStack.current
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
	val queue = playerState.queue

	val haptic = LocalHapticFeedback.current
	val draggableState = rememberDraggableListState(viewModel.listState) { from, to ->
		player.moveQueueItem(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	LaunchedEffect(playerState.currentIndex) {
		runCatching {
			if (queue.isNotEmpty()) {
				draggableState.listState.scrollToItem(
					playerState.currentIndex.coerceAtLeast(0)
				)
			}
		}
	}

	val preferenceManager = koinInject<PreferenceManager>()

	val songCountText = when (preferenceManager.queueInfoType) {
		QueueInfoType.Full -> pluralStringResource(
			Res.plurals.count_songs,
			queue.size,
			queue.size
		)

		QueueInfoType.Remaining -> pluralStringResource(
			Res.plurals.count_remaining_songs,
			queue.size - playerState.currentIndex,
			queue.size - playerState.currentIndex
		)
	}
	val durationText = remember(queue, playerState.progress, playerState.currentIndex) {
		var totalMillis = queue.sumOf { it.duration.inWholeMilliseconds } // ms because precision

		if (preferenceManager.queueInfoType == QueueInfoType.Remaining) {
			// duration of all songs that are before the current index
			val pastMillis = queue
				.take(playerState.currentIndex)
				.sumOf { it.duration.inWholeMilliseconds }

			// elapsed duration of the current index
			val currentTrack = queue.getOrNull(playerState.currentIndex)
			val elapsedCurrentSeconds =
				(currentTrack?.duration?.inWholeMilliseconds ?: 0L) * playerState.progress

			// deduct previous durations plus current elapsed duration
			totalMillis -= pastMillis + elapsedCurrentSeconds.toLong()

			// wow, that's confusing as shit for some reason
		}

		// then we format manually because Duration.toString() doesn't let u disable decimals
		val totalSeconds = totalMillis.milliseconds.inWholeSeconds
		val hours = totalSeconds / 3600
		val minutes = (totalSeconds % 3600) / 60
		val seconds = totalSeconds % 60

		buildString {
			if (hours > 0) append("${hours}h ")
			if (minutes > 0 || hours > 0) append("${minutes}m ")
			append("${seconds}s")
		}
	}
	val formattedDurationText = when (preferenceManager.queueInfoType) {
		QueueInfoType.Full -> durationText
		QueueInfoType.Remaining -> stringResource(Res.string.info_duration_left, durationText)
	}

	val sheetState = LocalSheetState.current
	val closeScope = rememberCoroutineScope()
	val animateToDismiss = {
		closeScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			if (!sheetState.isVisible) {
				backStack.remove(Screen.Queue)
			}
		}
	}

	Column(modifier = Modifier.fillMaxSize()) {
		if (queue.isNotEmpty()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Row(
					modifier = Modifier.height(36.dp).clickable {
						val newValue = when (preferenceManager.queueInfoType) {
							QueueInfoType.Full -> QueueInfoType.Remaining
							QueueInfoType.Remaining -> QueueInfoType.Full
						}
						preferenceManager.queueInfoType = newValue
					},
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "$songCountText • $formattedDurationText",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold,
						fontFamily = defaultFont(round = 100f),
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center
					)
				}
				FilledTonalButton(
					onClick = {
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
						player.clearQueue()
					},
					colors = ButtonDefaults.filledTonalButtonColors(),
					contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
					modifier = Modifier.height(36.dp)
				) {
					Text(stringResource(Res.string.action_clear_queue))
				}
			}
		}

		LazyColumn(
			modifier = Modifier
				.padding(horizontal = 12.dp)
				.fillMaxSize(),
			state = draggableState.listState,
			contentPadding = WindowInsets.systemBars
				.only(WindowInsetsSides.Bottom)
				.asPaddingValues(),
			verticalArrangement = if (queue.isNotEmpty())
				Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
			else Arrangement.Center
		) {
			draggableItemsIndexed(
				state = draggableState,
				items = queue,
				key = { index, _ -> index }
			) { index, song, isDragging ->
				QueueScreenItem(
					index = index,
					count = queue.count(),
					song = song,
					isPlaying = playerState.currentIndex == index
						&& !playerState.isPaused,
					isSelected = playerState.currentIndex == index,
					isDragging = isDragging,
					draggableState = draggableState,
					onClick = dropUnlessResumed {
						if (playerState.currentIndex != index) {
							player.playAt(index)
							animateToDismiss()
						}
					},
					onRemove = {
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
						player.removeFromQueue(index)
					},
					isOffline = !isOnline,
					isDownloaded = downloadedSongs.containsKey(song.id)
				)
			}
			if (queue.isEmpty()) {
				item {
					ContentUnavailable(
						icon = Icons.Outlined.PlaylistRemove,
						label = stringResource(Res.string.info_no_queue)
					)
				}
			}
		}
	}
}
