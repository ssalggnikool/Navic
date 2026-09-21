package paige.navic.ui.util

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.koin.compose.koinInject
import paige.navic.di.ResourceProvider

@Composable
actual fun playPauseIconPainter(reversed: Boolean): Painter? {
	val resourceProvider = koinInject<ResourceProvider>()
	val image = AnimatedImageVector.animatedVectorResource(resourceProvider.animPause)
	return rememberAnimatedVectorPainter(image, reversed)
}
