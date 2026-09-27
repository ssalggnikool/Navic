package paige.navic.util

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import coil3.request.ImageRequest

actual fun Modifier.backwardsCompatibleBlur(radius: Dp): Modifier {
	return this then Modifier.blur(radius)
}

@Composable
actual fun PlatformLayerComposable(
    modifier: Modifier,
    content: @Composable (() -> Unit)
) {
	Box(modifier) { content }
}

actual fun ImageRequest.Builder.disableHardwareIfCucked(): ImageRequest.Builder {
	return this
}
