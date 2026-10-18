/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.queue

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.settings.QueueInfoType
import paige.navic.playback.MediaPlayer
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.queue.component.QueueScreenItem
import paige.navic.ui.screen.queue.viewmodel.QueueViewModel
import paige.navic.ui.theme.defaultFont
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun QueueScreen() {
	val viewModel = koinViewModel<QueueViewModel>()
	val player = koinInject<MediaPlayer>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
	val queue = playerState.queue
	val haptic = LocalHapticFeedback.current
	val preferenceManager = koinInject<PreferenceManager>()
	val backStack = LocalNavStack.current

	val songCountText = when (preferenceManager.queueInfoType) {
		QueueInfoType.Full -> pluralStringResource(R.plurals.count_songs, queue.size, queue.size)
		QueueInfoType.Remaining -> pluralStringResource(
			R.plurals.count_remaining_songs,
			queue.size - playerState.currentIndex,
			queue.size - playerState.currentIndex
		)
	}
	val durationText = remember(queue, playerState.progress, playerState.currentIndex) {
		var totalMillis = queue.sumOf { it.duration.inWholeMilliseconds } // ms because precision

		if (preferenceManager.queueInfoType == QueueInfoType.Remaining) {
			// duration of all songs that are before the current index
			val pastMillis =
				queue.take(playerState.currentIndex).sumOf { it.duration.inWholeMilliseconds }

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
		QueueInfoType.Remaining -> stringResource(R.string.info_duration_left, durationText)
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
					modifier = Modifier
						.height(36.dp)
						.clickable {
							preferenceManager.queueInfoType =
								when (preferenceManager.queueInfoType) {
									QueueInfoType.Full -> QueueInfoType.Remaining
									QueueInfoType.Remaining -> QueueInfoType.Full
								}
						}, verticalAlignment = Alignment.CenterVertically
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
						backStack.remove(Screen.Queue)
						backStack.remove(Screen.NowPlaying)
					},
					colors = ButtonDefaults.filledTonalButtonColors(),
					contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
					modifier = Modifier.height(36.dp)
				) {
					Text(stringResource(R.string.action_clear_queue))
				}
			}
		}

		AndroidView(
			modifier = Modifier.padding(horizontal = 12.dp)
				.fillMaxSize()
				.navigationBarsPadding()
				.clipToBounds()
				.systemGestureExclusion {
					androidx.compose.ui.geometry.Rect(
						left = 5f,
						top = 0f,
						right = 5f,
						bottom = 0f
					)
				},
			factory = { context ->
				QueueRecyclerView(context).apply {
					layoutManager = LinearLayoutManager(context)
					isNestedScrollingEnabled = false
					addItemDecoration(object : RecyclerView.ItemDecoration() {
						override fun getItemOffsets(
							outRect: Rect,
							view: android.view.View,
							parent: RecyclerView,
							state: RecyclerView.State
						) {
							if (parent.getChildAdapterPosition(view) > 0) {
								outRect.top = (3 * resources.displayMetrics.density).toInt()
							}
						}
					})
					val adapter = QueueRecyclerAdapter()
					this.adapter = adapter
					val touchHelper = ItemTouchHelper(adapter.touchCallback)
					touchHelper.attachToRecyclerView(this)
					adapter.attachTouchHelper(touchHelper)
				}
			},
			update = { recyclerView ->
				val adapter = recyclerView.adapter as QueueRecyclerAdapter
				adapter.update(
					queue = queue,
					currentIndex = playerState.currentIndex,
					isPaused = playerState.isPaused,
					isOnline = isOnline,
					downloadedSongs = downloadedSongs,
					onPlay = { index ->
						if (playerState.currentIndex != index) {
							player.playAt(index)
						}
					},
					onRemove = { index ->
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
						player.removeFromQueue(index)
					},
					onMove = { from, to ->
						haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
						player.moveQueueItem(from, to)
					})
				if (adapter.shouldScrollToCurrentIndex) {
					recyclerView.scrollToPosition(playerState.currentIndex.coerceAtLeast(0))
					adapter.shouldScrollToCurrentIndex = false
				}
			})
	}
}

private class QueueRecyclerView(context: Context) : RecyclerView(context) {
	override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> parent?.requestDisallowInterceptTouchEvent(
				true
			)

			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(
				false
			)
		}
		return super.onInterceptTouchEvent(event)
	}
}

private class QueueRecyclerAdapter : RecyclerView.Adapter<QueueRecyclerAdapter.ViewHolder>() {
	private var queue = emptyList<DomainSong>()
	private var requestedQueue = emptyList<DomainSong>()
	private var currentIndex = 0
	private var isPaused = true
	private var isOnline = true
	private var downloadedSongs = emptyMap<String, String>()
	private var onPlay: (Int) -> Unit = {}
	private var onRemove: (Int) -> Unit = {}
	private var onMove: (Int, Int) -> Unit = { _, _ -> }
	private var draggingPosition = RecyclerView.NO_POSITION
	private var touchHelper: ItemTouchHelper? = null
	var shouldScrollToCurrentIndex = false

	init {
		setHasStableIds(true)
	}

	val touchCallback = object : ItemTouchHelper.SimpleCallback(
		ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
	) {
		override fun onMove(
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder,
			target: RecyclerView.ViewHolder
		): Boolean {
			val from = viewHolder.bindingAdapterPosition
			val to = target.bindingAdapterPosition
			if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
				return false
			}

			val mutableQueue = queue.toMutableList()
			mutableQueue.add(to, mutableQueue.removeAt(from))
			queue = mutableQueue
			requestedQueue = mutableQueue

			notifyItemMoved(from, to)
			onMove(from, to)
			return true
		}

		override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
			super.onSelectedChanged(viewHolder, actionState)
			draggingPosition =
				if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
					viewHolder.bindingAdapterPosition
				} else {
					RecyclerView.NO_POSITION
				}
			viewHolder?.itemView?.elevation =
				if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) 8f else 0f
		}

		override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
			super.clearView(recyclerView, viewHolder)
			draggingPosition = RecyclerView.NO_POSITION
			viewHolder.itemView.elevation = 0f
		}

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
	}

	fun attachTouchHelper(touchHelper: ItemTouchHelper) {
		this.touchHelper = touchHelper
	}

	@SuppressLint("NotifyDataSetChanged")
	fun update(
		queue: List<DomainSong>,
		currentIndex: Int,
		isPaused: Boolean,
		isOnline: Boolean,
		downloadedSongs: Map<String, String>,
		onPlay: (Int) -> Unit,
		onRemove: (Int) -> Unit,
		onMove: (Int, Int) -> Unit
	) {
		val queueChanged = requestedQueue != queue
		val itemStateChanged =
			this.currentIndex != currentIndex || this.isPaused != isPaused || this.isOnline != isOnline || this.downloadedSongs != downloadedSongs
		shouldScrollToCurrentIndex = this.currentIndex != currentIndex
		requestedQueue = queue
		this.currentIndex = currentIndex
		this.isPaused = isPaused
		this.isOnline = isOnline
		this.downloadedSongs = downloadedSongs
		this.onPlay = onPlay
		this.onRemove = onRemove
		this.onMove = onMove
		if (queueChanged) {
			this.queue = queue
			notifyDataSetChanged()
		} else if (itemStateChanged) {
			notifyItemRangeChanged(0, itemCount)
		}
	}

	override fun getItemCount() = if (queue.isEmpty()) 1 else queue.size

	override fun getItemId(position: Int) =
		if (queue.isEmpty()) 0L else queue[position].id.hashCode().toLong()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(ComposeView(parent.context).apply {
			layoutParams = RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
			)
			setViewCompositionStrategy(
				ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
			)
		})
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (queue.isEmpty()) {
			holder.view.setContent {
				ContentUnavailable(label = stringResource(R.string.info_no_queue))
			}
			return
		}
		val song = queue[position]
		holder.view.setContent {
			QueueScreenItem(
				index = position,
				count = queue.size,
				song = song,
				isPlaying = currentIndex == position && !isPaused,
				isSelected = currentIndex == position,
				isDragging = draggingPosition == position,
				onDrag = { touchHelper?.startDrag(holder) },
				onClick = {
					holder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
						?.let(onPlay)
				},
				onRemove = {
					holder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
						?.let(onRemove)
				},
				isOffline = !isOnline,
				isDownloaded = downloadedSongs.containsKey(song.id)
			)
		}
	}

	class ViewHolder(val view: ComposeView) : RecyclerView.ViewHolder(view)
}
