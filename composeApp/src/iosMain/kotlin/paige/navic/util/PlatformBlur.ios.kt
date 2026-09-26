package paige.navic.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp

actual fun Modifier.backwardsCompatibleBlur(radius: Dp): Modifier {
	return this then Modifier.blur(radius)
}
