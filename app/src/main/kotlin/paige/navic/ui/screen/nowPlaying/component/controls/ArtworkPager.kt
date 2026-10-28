/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying.component.controls

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.CoverArtTapAction
import paige.navic.playback.MediaPlayer
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.nowPlaying.component.NowPlayingArtwork

@Composable
fun NowPlayingArtworkPager(
	modifier: Modifier = Modifier,
	isLandscape: Boolean
) {
	val backStack = LocalNavStack.current
	val preferenceManager = koinInject<PreferenceManager>()
	val player = koinInject<MediaPlayer>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val queue = remember(playerState.queue, playerState.shuffleOrder) {
		playerState.activeQueue
	}
	val currentIndex = remember(
		playerState.queue,
		playerState.shuffleOrder,
		playerState.currentIndex
	) {
		playerState.activeIndex
	}

	val pagerState = rememberPagerState(
		initialPage = currentIndex.coerceAtLeast(0),
		pageCount = { queue.size }
	)
	val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
	var userSwiped by remember { mutableStateOf(false) }

	LaunchedEffect(isDragged) {
		if (isDragged) userSwiped = true
	}

	LaunchedEffect(currentIndex) {
		if (!isDragged && currentIndex != -1 && currentIndex != pagerState.currentPage) {
			pagerState.animateScrollToPage(currentIndex)
		}
	}

	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.settledPage }.collect { page ->
			val state = player.uiState.value
			if (userSwiped && page != state.activeIndex && page in state.activeQueue.indices) {
				val wasPaused = state.isPaused
				player.playAt(page)
				if (wasPaused) {
					player.pause()
				}
			}
			userSwiped = false
		}
	}

	HorizontalPager(
		modifier = modifier,
		state = pagerState,
		contentPadding = PaddingValues(horizontal = if (isLandscape) 0.dp else 8.dp),
		userScrollEnabled = preferenceManager.swipeToSkip,
		overscrollEffect = null
	) { page ->
		val song = queue.getOrNull(page) ?: return@HorizontalPager
		val tapAction = preferenceManager.nowPlayingCoverArtAction
		val enabled = pagerState.settledPage == page
			&& tapAction != CoverArtTapAction.Disabled
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			NowPlayingArtwork(
				song = song,
				isLandscape = isLandscape,
				onClick = if (enabled) dropUnlessResumed {
					when (tapAction) {
						CoverArtTapAction.ShowLyrics -> backStack.add(Screen.Lyrics)
					}
				} else null
			)
		}
	}
}
