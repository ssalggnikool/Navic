/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package paige.navic.ui.screen.queue

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.R
import paige.navic.designsystem.theme.defaultFont
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSetSheetGesturesEnabled
import paige.navic.di.LocalSheetState
import paige.navic.domain.manager.ui.SnackBarManager
import paige.navic.domain.model.settings.QueueInfoType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.playback.PlayerRepository
import paige.navic.preferences.PreferenceManager
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.snackbar.NavicSnackBar
import paige.navic.ui.component.snackbar.SwipeableSnackBarHost
import paige.navic.ui.core.QueueSong
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.playlist.dialog.PlaylistCreateDialog
import paige.navic.ui.screen.queue.component.QueueScreenItem
import paige.navic.ui.screen.queue.viewmodel.QueueViewModel
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun QueueScreen() {
	val viewModel = koinViewModel<QueueViewModel>()
	val player = koinInject<PlayerRepository>()
	val state = player.uiState.collectAsStateWithLifecycle()

	val playerState by remember(player) {
		player.uiState
			.map { it.copy(progress = 0f) }
			.distinctUntilChanged()
	}.collectAsStateWithLifecycle(
		initialValue = state.value.copy(progress = 0f)
	)
	val sheetState = LocalSheetState.current
	val setSheetGesturesEnabled = LocalSetSheetGesturesEnabled.current

	val isReachable by viewModel.isReachable.collectAsStateWithLifecycle()
	val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
	val queue = playerState.activeQueue
	val haptic = LocalHapticFeedback.current
	val preferenceManager = koinInject<PreferenceManager>()
	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	val snackBarState = remember { SnackbarHostState() }
	val snackBarManager = koinInject<SnackBarManager>()
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		snackBarManager.events.collectLatest { event ->
			val actionLabelRes = event.actionLabelRes
			val actionLabel = actionLabelRes?.let { context.getString(it) }
			val duration = if (actionLabelRes != null) {
				SnackbarDuration.Long
			} else {
				SnackbarDuration.Short
			}
			val result = snackBarState.showSnackbar(
				message = with(context) { resources.getString(event.resource, *event.args.toTypedArray()) },
				actionLabel = actionLabel,
				duration = duration
			)
			if (result == SnackbarResult.ActionPerformed) {
				event.onAction?.invoke(context)
			}
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		Column(modifier = Modifier.fillMaxSize()) {
			if (queue.isNotEmpty()) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp, vertical = 8.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						modifier = Modifier
							.weight(1f)
							.clickable {
								preferenceManager.queueInfoType =
									when (preferenceManager.queueInfoType) {
										QueueInfoType.Full -> QueueInfoType.Remaining
										QueueInfoType.Remaining -> QueueInfoType.Full
									}
							},
						verticalAlignment = Alignment.CenterVertically
					) {
						QueueInfoText(
							player = player,
							queue = queue,
							currentIndex = playerState.activeIndex
						)
					}
					Row(
						modifier = Modifier.padding(start = 8.dp),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						IconButton(
							onClick = { playlistDialogShown = true },
							colors = IconButtonDefaults.filledTonalIconButtonColors(),
							modifier = Modifier.height(36.dp)
						) {
							Icon(
								Icons.Outlined.PlaylistAdd,
								contentDescription = stringResource(R.string.action_save_as_playlist),
								modifier = Modifier.size(20.dp)
							)
						}
						FilledTonalButton(
							onClick = {
								haptic.performHapticFeedback(HapticFeedbackType.LongPress)
								val currentSize = queue.size
								player.clearQueue()
								if (currentSize <= 1) {
									backStack.remove(Screen.Queue)
									backStack.remove(Screen.NowPlaying)
								}
							},
							colors = ButtonDefaults.filledTonalButtonColors(),
							contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
							modifier = Modifier.height(36.dp)
						) {
							Text(stringResource(R.string.action_clear_queue))
						}
					}
				}
			}

			AndroidView(
				modifier = Modifier
					.padding(horizontal = 12.dp)
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
					QueueRecyclerView(context, sheetState, setSheetGesturesEnabled).apply {
						overScrollMode = View.OVER_SCROLL_NEVER
						layoutManager = LinearLayoutManager(context)
						isNestedScrollingEnabled = true
						clipToOutline = true
						addItemDecoration(object : RecyclerView.ItemDecoration() {
							override fun getItemOffsets(
								outRect: Rect,
								view: View,
								parent: RecyclerView,
								state: RecyclerView.State
							) {
								if (parent.getChildAdapterPosition(view) > 0) {
									outRect.top = (3 * resources.displayMetrics.density).toInt()
								}
							}
						})
						val adapter = QueueRecyclerAdapter(
							onDragStateChanged = { dragging ->
								isItemDragging = dragging
								parent?.requestDisallowInterceptTouchEvent(dragging)
								setSheetGesturesEnabled(!dragging)
							}
						)
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
						currentIndex = playerState.activeIndex,
						isPaused = playerState.isPaused,
						isOnline = isReachable,
						downloadedSongs = downloadedSongs,
						onPlay = { index ->
							if (playerState.activeIndex != index) {
								player.playAt(playerState.normalIndex(index))
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
						recyclerView.scrollToPosition(playerState.activeIndex.coerceAtLeast(0))
						adapter.shouldScrollToCurrentIndex = false
					}
				})
		}

		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding()
				.padding(bottom = 16.dp)
		) {
			SwipeableSnackBarHost(
				hostState = snackBarState
			) { snackBarData, offset ->
				NavicSnackBar(
					snackBarData = snackBarData,
					modifier = Modifier.offset(x = offset)
				)
			}
		}
	}

	if (playlistDialogShown) {
		PlaylistCreateDialog(
			songIds = queue.map { it.id }.toPersistentList(),
			onDismissRequest = { playlistDialogShown = false },
			onRefresh = {}
		)
	}
}

@Composable
private fun QueueInfoText(
	player: PlayerRepository,
	queue: List<QueueSong>,
	currentIndex: Int
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val queueInfoType = preferenceManager.queueInfoType
	val state = player.uiState.collectAsStateWithLifecycle()

	val songCountText = when (queueInfoType) {
		QueueInfoType.Full -> pluralStringResource(R.plurals.count_songs, queue.size, queue.size)
		QueueInfoType.Remaining -> pluralStringResource(
			R.plurals.count_remaining_songs,
			queue.size - currentIndex,
			queue.size - currentIndex
		)
	}
	val totalMillis = remember(queue) {
		queue.sumOf { it.duration.inWholeMilliseconds } // ms because precision
	}
	// duration of all songs that are before the current index
	val pastMillis = remember(queue, currentIndex) {
		queue.take(currentIndex.coerceIn(0, queue.size))
			.sumOf { it.duration.inWholeMilliseconds }
	}
	val currentTrackMillis = remember(queue, currentIndex) {
		queue.getOrNull(currentIndex)?.duration?.inWholeMilliseconds ?: 0L
	}

	val elapsedMillis by remember(player, currentTrackMillis) {
		player.uiState.map {
			(it.progress * currentTrackMillis).toLong() / 1000 * 1000
		}.distinctUntilChanged()
	}.collectAsStateWithLifecycle(
		initialValue =
			(state.value.progress * currentTrackMillis).toLong() / 1000 * 1000
	)
	val durationText = remember(
		queueInfoType,
		totalMillis,
		pastMillis,
		elapsedMillis
	) {
		var remainingMillis = totalMillis

		if (queueInfoType == QueueInfoType.Remaining) {
			// deduct previous durations plus current elapsed duration
			remainingMillis -= pastMillis + elapsedMillis

			// wow, that's confusing as shit for some reason
		}

		// then we format manually because Duration.toString() doesn't let u disable decimals
		val totalSeconds = remainingMillis.milliseconds.inWholeSeconds
		val hours = totalSeconds / 3600
		val minutes = (totalSeconds % 3600) / 60
		val seconds = totalSeconds % 60

		buildString {
			if (hours > 0) append("${hours}h ")
			if (minutes > 0 || hours > 0) append("${minutes}m ")
			append("${seconds}s")
		}
	}
	val formattedDurationText = when (queueInfoType) {
		QueueInfoType.Full -> durationText
		QueueInfoType.Remaining -> stringResource(R.string.info_duration_left, durationText)
	}

	Column {
		Text(
			text = songCountText,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.SemiBold,
			fontFamily = defaultFont(round = 100f),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Start,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
		Text(
			text = formattedDurationText,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.SemiBold,
			fontFamily = defaultFont(round = 100f),
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
			textAlign = TextAlign.Start,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

// this almost helped me, almost...
// https://stackoverflow.com/questions/56012918/recyclerview-in-bottomsheet-not-working-as-expected
@SuppressLint("ViewConstructor")
private class QueueRecyclerView(
	context: Context,
	private val sheetState: SheetState,
	private val setSheetGesturesEnabled: (Boolean) -> Unit
) : RecyclerView(context) {
	private var lastY = 0f
	var isItemDragging = false

	override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
		if (isItemDragging) {
			parent?.requestDisallowInterceptTouchEvent(true)
			setSheetGesturesEnabled(false)
			return super.onInterceptTouchEvent(event)
		}

		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN -> {
				lastY = event.y
				parent?.requestDisallowInterceptTouchEvent(
					canScrollVertically(-1) || canScrollVertically(1)
				)
			}
			MotionEvent.ACTION_MOVE -> {
				val draggingDown = event.y - lastY > 0
				lastY = event.y

				if (draggingDown && !canScrollVertically(-1)) {
					parent?.requestDisallowInterceptTouchEvent(false)
					setSheetGesturesEnabled(true)
				} else {
					setSheetGesturesEnabled(false)
				}
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
				parent?.requestDisallowInterceptTouchEvent(false)
		}
		return super.onInterceptTouchEvent(event)
	}

	override fun startNestedScroll(axes: Int, type: Int): Boolean =
		!isItemDragging && !sheetState.isAnimationRunning && super.startNestedScroll(axes, type)
}

private class QueueRecyclerAdapter(
	private val onDragStateChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<QueueRecyclerAdapter.ViewHolder>() {
	private var queue = emptyList<QueueSong>()
	private var currentIndex = 0
	private var isPaused = true
	private var isOnline = true
	private var downloadedSongs = emptyMap<String, String>()
	private var onPlay: (Int) -> Unit = {}
	private var onRemove: (Int) -> Unit = {}
	private var onMove: (Int, Int) -> Unit = { _, _ -> }
	private var dragStartPosition = RecyclerView.NO_POSITION
	private var draggingPosition = RecyclerView.NO_POSITION
	private var touchHelper: ItemTouchHelper? = null
	var shouldScrollToCurrentIndex = false


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
			if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION || from == to) {
				return false
			}

			val mutableQueue = queue.toMutableList()
			mutableQueue.add(to, mutableQueue.removeAt(from))
			queue = mutableQueue

			currentIndex = when (currentIndex) {
				from -> to
				in (from + 1)..to -> currentIndex - 1
				in to..<from -> currentIndex + 1
				else -> currentIndex
			}

			draggingPosition = to
			notifyItemMoved(from, to)

			(viewHolder as? ViewHolder)?.boundIndex = to
			(target as? ViewHolder)?.boundIndex = from

			return true
		}

		override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
			super.onSelectedChanged(viewHolder, actionState)
			if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
				onDragStateChanged(true)
				dragStartPosition = viewHolder.bindingAdapterPosition
				draggingPosition = dragStartPosition
				(viewHolder as? ViewHolder)?.isItemDragging = true
				viewHolder.itemView.elevation = 8f
			}
		}

		override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
			super.clearView(recyclerView, viewHolder)
			onDragStateChanged(false)
			viewHolder.itemView.elevation = 0f
			(viewHolder as? ViewHolder)?.isItemDragging = false

			val from = dragStartPosition
			val to = viewHolder.bindingAdapterPosition

			dragStartPosition = RecyclerView.NO_POSITION
			draggingPosition = RecyclerView.NO_POSITION

			for (i in 0 until recyclerView.childCount) {
				val child = recyclerView.getChildAt(i)
				val holder = recyclerView.getChildViewHolder(child) as? ViewHolder
				if (holder != null && holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
					holder.boundIndex = holder.bindingAdapterPosition
				}
			}

			if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION && from != to) {
				onMove(from, to)
			}
		}

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
	}

	fun attachTouchHelper(touchHelper: ItemTouchHelper) {
		this.touchHelper = touchHelper
	}

	@SuppressLint("NotifyDataSetChanged")
	fun update(
		queue: List<QueueSong>,
		currentIndex: Int,
		isPaused: Boolean,
		isOnline: Boolean,
		downloadedSongs: Map<String, String>,
		onPlay: (Int) -> Unit,
		onRemove: (Int) -> Unit,
		onMove: (Int, Int) -> Unit
	) {
		this.onPlay = onPlay
		this.onRemove = onRemove
		this.onMove = onMove

		if (draggingPosition != RecyclerView.NO_POSITION) return

		val queueChanged = this.queue != queue
		val itemStateChanged =
			this.currentIndex != currentIndex ||
				this.isPaused != isPaused ||
				this.isOnline != isOnline ||
				this.downloadedSongs != downloadedSongs

		shouldScrollToCurrentIndex = this.currentIndex != currentIndex
		this.currentIndex = currentIndex
		this.isPaused = isPaused
		this.isOnline = isOnline
		this.downloadedSongs = downloadedSongs

		if (queueChanged) {
			this.queue = queue
			notifyDataSetChanged()
		} else if (itemStateChanged) {
			notifyItemRangeChanged(0, itemCount)
		}
	}

	override fun getItemCount() = if (queue.isEmpty()) 1 else queue.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val composeView = ComposeView(parent.context)
		return ViewHolder(composeView)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (queue.isEmpty()) {
			holder.view.setContent {
				ContentUnavailable(label = stringResource(R.string.info_no_queue))
			}
			return
		}
		holder.boundIndex = position
		holder.isItemDragging = draggingPosition == position
		val song = queue[position]
		holder.view.setContent {
			val liveIndex = holder.boundIndex.takeIf { it != RecyclerView.NO_POSITION } ?: position
			QueueScreenItem(
				index = liveIndex,
				count = queue.size,
				song = song,
				isPlaying = currentIndex == liveIndex && !isPaused,
				isSelected = currentIndex == liveIndex,
				isDragging = holder.isItemDragging,
				onDrag = {
					onDragStateChanged(true)
					touchHelper?.startDrag(holder)
				},
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

	class ViewHolder(val view: ComposeView) : RecyclerView.ViewHolder(view) {
		var boundIndex by mutableIntStateOf(RecyclerView.NO_POSITION)
		var isItemDragging by mutableStateOf(false)
	}
}
