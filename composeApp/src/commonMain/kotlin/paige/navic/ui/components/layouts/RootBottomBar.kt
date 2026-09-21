package paige.navic.ui.components.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import org.koin.compose.koinInject
import paige.navic.di.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.BottomBarCollapseMode
import paige.navic.domain.models.settings.MiniPlayerStyle
import paige.navic.ui.util.easedVerticalGradient

@Composable
fun RootBottomBar(
	scrolled: Boolean,
	modifier: Modifier = Modifier,
	shadows: Boolean = true,
	hideMiniPlayer: Boolean = false,
	windowInsets: WindowInsets = NavigationBarDefaults.windowInsets
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val platformContext = LocalPlatformContext.current
	val scrolled =
		scrolled && preferenceManager.bottomBarCollapseMode == BottomBarCollapseMode.OnScroll
	val detached = preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached
	val hideBottomBar = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
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
	Column(
		modifier = modifier.then(
			if (detached)
				Modifier.background(
					Brush.easedVerticalGradient(color = MaterialTheme.colorScheme.surface.copy(alpha = shadowFadeProgress))
				)
			else Modifier
		)
	) {
		if (!hideMiniPlayer) {
			MiniPlayer(
				modifier = Modifier.graphicsLayer {
					alpha = progress.coerceIn(0f..1f)
					translationY = ((1f - progress) * (size.height * 2)).coerceAtLeast(
						if (detached) -2048f else 0f
					)
				},
				enabled = !scrolled,
				windowInsets = if (!hideBottomBar)
					windowInsets.only(WindowInsetsSides.Horizontal)
				else windowInsets
			)
		}
		if (!hideBottomBar) {
			BottomBar(
				containerColor = if (detached)
					NavigationBarDefaults.containerColor.copy(alpha = 0f)
				else NavigationBarDefaults.containerColor,
				modifier = Modifier.graphicsLayer {
					alpha = progress.coerceIn(0f..1f)
					translationY = ((1f - progress) * size.height).coerceAtLeast(
						if (preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached) -2048f else 0f
					)
				},
				enabled = !scrolled,
				windowInsets = windowInsets
			)
		}
	}
}
