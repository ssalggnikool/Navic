/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons

val Icons.Filled.BottomNavigation: ImageVector
	get() {
		if (_BottomNavigation != null) {
			return _BottomNavigation!!
		}
		_BottomNavigation = ImageVector.Builder(
			name = "Filled.BottomNavigation",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(200f, 840f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(120f, 760f)
				verticalLineToRelative(-560f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(200f, 120f)
				horizontalLineToRelative(560f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(840f, 200f)
				verticalLineToRelative(560f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(760f, 840f)
				lineTo(200f, 840f)
				close()
				moveTo(200f, 600f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-400f)
				lineTo(200f, 200f)
				verticalLineToRelative(400f)
				close()
			}
		}.build()

		return _BottomNavigation!!
	}

@Suppress("ObjectPropertyName")
private var _BottomNavigation: ImageVector? = null
