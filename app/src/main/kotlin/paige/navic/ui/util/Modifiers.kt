/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.shimmerLoading(
	durationMillis: Int = 1100,
): Modifier {
	val transition = rememberInfiniteTransition(label = "")

	val translateAnimation by transition.animateFloat(
		initialValue = -200f,
		targetValue = 600f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = durationMillis,
				easing = LinearEasing,
			),
			repeatMode = RepeatMode.Restart,
		),
		label = "",
	)

	return drawBehind {
		drawRect(
			brush = Brush.linearGradient(
				colors = listOf(
					Color.LightGray.copy(alpha = 0.1f),
					Color.LightGray.copy(alpha = 0.2f),
					Color.LightGray.copy(alpha = 0.1f),
				),
				start = Offset(x = translateAnimation, y = translateAnimation),
				end = Offset(x = translateAnimation + 200f, y = translateAnimation + 200f),
			)
		)
	}
}

/**
 * cheap approximation of an elevation shadow
 */
fun Modifier.fakeShadow(
	elevation: Dp,
	rotationZ: Float = 0f,
	cornerRadius: Dp = 12.dp,
	shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier {
	if (elevation <= 0.dp) return this
	return drawBehind {
		val dy = elevation.toPx() * 0.5f
		val alpha = (0.10f + elevation.value * 0.008f).coerceAtMost(0.20f)
		rotate(rotationZ, pivot = center) {
			translate(top = dy) {
				drawOutline(
					outline = shape.createOutline(size, layoutDirection, this),
					color = Color.Black.copy(alpha = alpha)
				)
			}
		}
	}
}
