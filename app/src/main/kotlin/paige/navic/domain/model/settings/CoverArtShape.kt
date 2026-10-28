/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class CoverArtShape(
	val shape: Shape,
	val decreasedShape: Shape = shape
) {
	Square(RectangleShape),
	Soft(RoundedCornerShape(10.dp), RoundedCornerShape(8.dp)),
	Curved(RoundedCornerShape(32.dp), RoundedCornerShape(10.dp)),
	Circle(CircleShape)
}
