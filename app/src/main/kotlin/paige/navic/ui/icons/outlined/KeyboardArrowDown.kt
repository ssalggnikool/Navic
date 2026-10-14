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

val Icons.Outlined.KeyboardArrowDown: ImageVector
	get() {
		if (_KeyboardArrowDown != null) {
			return _KeyboardArrowDown!!
		}
		_KeyboardArrowDown = ImageVector.Builder(
			name = "Outlined.KeyboardArrowDown",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(465f, 596.5f)
				quadToRelative(-7f, -2.5f, -13f, -8.5f)
				lineTo(268f, 404f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(28f, 11f)
				lineToRelative(156f, 156f)
				lineToRelative(156f, -156f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(28f, 11f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				lineTo(508f, 588f)
				quadToRelative(-6f, 6f, -13f, 8.5f)
				reflectiveQuadToRelative(-15f, 2.5f)
				quadToRelative(-8f, 0f, -15f, -2.5f)
				close()
			}
		}.build()

		return _KeyboardArrowDown!!
	}

@Suppress("ObjectPropertyName")
private var _KeyboardArrowDown: ImageVector? = null
