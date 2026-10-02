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

val Icons.Outlined.Close: ImageVector
	get() {
		if (_Close != null) {
			return _Close!!
		}
		_Close = ImageVector.Builder(
			name = "Outlined.Close",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(480f, 536f)
				lineTo(284f, 732f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				lineToRelative(196f, -196f)
				lineToRelative(-196f, -196f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(28f, 11f)
				lineToRelative(196f, 196f)
				lineToRelative(196f, -196f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(28f, 11f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				lineTo(536f, 480f)
				lineToRelative(196f, 196f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				lineTo(480f, 536f)
				close()
			}
		}.build()

		return _Close!!
	}

@Suppress("ObjectPropertyName")
private var _Close: ImageVector? = null
