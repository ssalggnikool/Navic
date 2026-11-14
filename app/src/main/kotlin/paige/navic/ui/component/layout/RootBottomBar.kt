/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.BottomBarCollapseMode
import paige.navic.domain.model.settings.MiniPlayerStyle
import paige.navic.playback.PlayerRepository
import paige.navic.ui.util.easedVerticalGradient

@Composable
fun RootBottomBar(
	scrolled: Boolean,
	modifier: Modifier = Modifier,
	shadows: Boolean = true,
	windowInsets: WindowInsets = WindowInsets.systemBars
) {
	val sizeClass = LocalSizeClass.current
	if (sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) return

	val preferenceManager = koinInject<PreferenceManager>()
	val scrolled =
		scrolled && preferenceManager.bottomBarCollapseMode == BottomBarCollapseMode.OnScroll
	val detached = preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached
	val progress by animateFloatAsState(
		targetValue = if (scrolled) 0f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioLowBouncy,
			stiffness = Spring.StiffnessMediumLow
		)
	)
	val shadowFadeProgress by animateFloatAsState(
		targetValue = if (scrolled || !shadows) 0f else 1f,
		animationSpec = tween(durationMillis = 600)
	)

	val player = koinInject<PlayerRepository>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val hasSong = playerState.currentSong != null

	Column(
		modifier = modifier.then(
			if (detached)
				Modifier.background(
					Brush.easedVerticalGradient(color = MaterialTheme.colorScheme.surface.copy(alpha = shadowFadeProgress))
				)
			else Modifier
		)
	) {
		AnimatedVisibility(
			visible = hasSong || !preferenceManager.hideIfIdle,
			modifier = Modifier.fillMaxWidth().graphicsLayer {
				alpha = progress.coerceIn(0f..1f)
				translationY = ((1f - progress) * (size.height * 2)).coerceAtLeast(
					if (detached) -2048f else 0f
				)
			}
		) {
			Swiper(
				onSwipeLeft = { if (hasSong) player.previous() },
				onSwipeRight = { if (hasSong) player.next() },
				onSwipeDown = { player.clearQueue() },
				swipeLeftAccessibilityLabel = stringResource(R.string.action_previous_song),
				swipeRightAccessibilityLabel = stringResource(R.string.action_next_song),
				enabled = !scrolled
			) {
				MiniPlayer(enabled = !scrolled)
			}
		}
		BottomBar(
			containerColor = if (detached)
				NavigationBarDefaults.containerColor.copy(alpha = 0f)
			else NavigationBarDefaults.containerColor,
			modifier = Modifier.graphicsLayer {
				alpha = progress.coerceIn(0f..1f)
				translationY = ((1f - progress) * size.height).coerceAtLeast(
					if (detached) -2048f else 0f
				)
			},
			enabled = !scrolled,
			windowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
		)
	}
}
