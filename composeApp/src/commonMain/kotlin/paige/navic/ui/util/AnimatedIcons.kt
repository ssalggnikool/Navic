package paige.navic.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
expect fun playPauseIconPainter(reversed: Boolean): Painter?
