/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons

val Icons.Outlined.ArrowDown: ImageVector
	get() {
		if (_ArrowDown != null) {
			return _ArrowDown!!
		}
		_ArrowDown = ImageVector.Builder(
			name = "Outlined.ArrowDown",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(440f, 647f)
				verticalLineToRelative(-447f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(480f, 160f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(520f, 200f)
				verticalLineToRelative(447f)
				lineToRelative(196f, -196f)
				quadToRelative(12f, -12f, 28f, -11.5f)
				reflectiveQuadToRelative(28f, 12.5f)
				quadToRelative(11f, 12f, 11.5f, 28f)
				reflectiveQuadTo(772f, 508f)
				lineTo(508f, 772f)
				quadToRelative(-6f, 6f, -13f, 8.5f)
				reflectiveQuadToRelative(-15f, 2.5f)
				quadToRelative(-8f, 0f, -15f, -2.5f)
				reflectiveQuadToRelative(-13f, -8.5f)
				lineTo(188f, 508f)
				quadToRelative(-11f, -11f, -11f, -27.5f)
				reflectiveQuadToRelative(11f, -28.5f)
				quadToRelative(12f, -12f, 28.5f, -12f)
				reflectiveQuadToRelative(28.5f, 12f)
				lineToRelative(195f, 195f)
				close()
			}
		}.build()

		return _ArrowDown!!
	}

@Suppress("ObjectPropertyName")
private var _ArrowDown: ImageVector? = null
