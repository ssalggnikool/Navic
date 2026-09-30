/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.util

import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Brush.Companion.easedGradient(
	easing: Easing = EaseInCirc,
	start: Offset = Offset.Zero,
	end: Offset = Offset.Infinite,
	numStops: Int = 16,
	color: Color = Color.Black
): Brush {
	val colors = List(numStops) { i ->
		val x = i * 1f / (numStops - 1)
		color.copy(alpha = (1f - easing.transform(x)) * color.alpha)
	}

	return linearGradient(colors = colors, start = start, end = end)
}

fun Brush.Companion.easedVerticalGradient(
	easing: Easing = EaseInCirc,
	startY: Float = Float.POSITIVE_INFINITY,
	endY: Float = 0.0f,
	numStops: Int = 16,
	color: Color = Color.Black
): Brush = easedGradient(
	easing = easing,
	numStops = numStops,
	start = Offset(x = 0f, y = startY),
	end = Offset(x = 0f, y = endY),
	color = color
)
