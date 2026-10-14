/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.util

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import paige.navic.R

@Composable
fun playPauseIconPainter(reversed: Boolean): Painter {
	val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_pause)
	return rememberAnimatedVectorPainter(image, reversed)
}
