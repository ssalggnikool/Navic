/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import paige.navic.ui.util.DurationLong1
import paige.navic.ui.util.DurationLong2
import paige.navic.ui.util.EmphasizedEasing
import paige.navic.ui.util.rememberScreenCornerRadius

val HorizontalPagerAnimationSpec = tween<Float>(
	durationMillis = DurationLong2,
	easing = EmphasizedEasing
)

@Composable
fun NavigableHorizontalPager(
	modifier: Modifier = Modifier,
	state: PagerState,
	userScrollEnabled: Boolean = true,
	pageContent: @Composable (page: Int, pageModifier: Modifier) -> Unit
) {
	val navigationState = rememberNavigationEventState(NavigationEventInfo.None)
	var animationActive by rememberSaveable { mutableStateOf(false) }
	// previous values prior to when the back animation started
	var initialTouchX by rememberSaveable { mutableFloatStateOf(0f) }
	var initialPage by rememberSaveable { mutableIntStateOf(0) }
	var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
	var progress by rememberSaveable { mutableFloatStateOf(0f) }
	var previousProgress by rememberSaveable { mutableFloatStateOf(0f) }
	val pagerScope = rememberCoroutineScope()
	val onBackCancelled = {
		// because reading initialPage in .launch will capture an outdated value
		val pageToRestore = initialPage
		pagerScope.launch {
			state.animateScrollToPage(pageToRestore, animationSpec = HorizontalPagerAnimationSpec)
		}
		animationActive = false
		initialTouchX = 0f
		initialPage = 0
		offsetX = 0f
		progress = 0f
		previousProgress = 0f
	}

	val focusManager = LocalFocusManager.current

	NavigationBackHandler(
		state = navigationState,
		isBackEnabled = state.currentPage != 0 && userScrollEnabled,
		onBackCompleted = {
			progress = 0f
			focusManager.clearFocus(true)
			pagerScope.launch {
				state.animateScrollToPage(
					state.currentPage - 1,
					animationSpec = tween(
						durationMillis = DurationLong1,
						easing = EmphasizedEasing
					)
				)
			}
		},
		onBackCancelled = onBackCancelled
	)

	LaunchedEffect(navigationState.transitionState) {
		when (val transitionState = navigationState.transitionState) {
			is NavigationEventTransitionState.InProgress -> {
				if (!animationActive) {
					initialTouchX = transitionState.latestEvent.touchX
					initialPage = state.settledPage
				}
				offsetX = if (transitionState.latestEvent.swipeEdge == EDGE_RIGHT) {
					(transitionState.latestEvent.touchX - initialTouchX)
				} else {
					transitionState.latestEvent.touchX
				}
				progress = transitionState.latestEvent.progress
				animationActive = true
				val pageSize = state.layoutInfo.pageSize.toFloat()
				state.scroll(MutatePriority.PreventUserInput) {
					val clampedProgress = progress.coerceIn(0f, 0.2f)
					val delta = clampedProgress - previousProgress
					scrollBy(-delta * pageSize)
					previousProgress = clampedProgress
				}
			}

			else -> {}
		}
	}

	val pageScale by animateFloatAsState(1 - (progress.coerceAtMost(0.1f)))
	val screenShape = RoundedCornerShape(rememberScreenCornerRadius())
	val pageModifier = Modifier
		.scale(pageScale)
		.then(
			if (pageScale < 1f)
				Modifier
					.clip(screenShape)
			else Modifier
		)
	val pagerBackground = Color(
		red = MaterialTheme.colorScheme.background.red * 0.9f,
		green = MaterialTheme.colorScheme.background.green * 0.9f,
		blue = MaterialTheme.colorScheme.background.blue * 0.9f,
		alpha = MaterialTheme.colorScheme.background.alpha
	)

	HorizontalPager(
		modifier = modifier.background(pagerBackground),
		state = state,
		userScrollEnabled = false,
		pageContent = { page ->
			pageContent(page, pageModifier)
		}
	)
}
