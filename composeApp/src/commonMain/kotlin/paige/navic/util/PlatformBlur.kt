package paige.navic.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import coil3.request.ImageRequest

expect fun Modifier.backwardsCompatibleBlur(radius: Dp): Modifier

@Composable
expect fun PlatformLayerComposable(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
)

expect fun ImageRequest.Builder.disableHardwareIfCucked(): ImageRequest.Builder
