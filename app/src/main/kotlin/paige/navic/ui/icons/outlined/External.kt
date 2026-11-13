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

val Icons.Outlined.External: ImageVector
	get() {
		if (_External != null) {
			return _External!!
		}
		_External = ImageVector.Builder(
			name = "External",
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
				horizontalLineToRelative(240f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(480f, 160f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(440f, 200f)
				lineTo(200f, 200f)
				verticalLineToRelative(560f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-240f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(800f, 480f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(840f, 520f)
				verticalLineToRelative(240f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(760f, 840f)
				lineTo(200f, 840f)
				close()
				moveTo(760f, 256f)
				lineTo(416f, 600f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				lineToRelative(344f, -344f)
				lineTo(600f, 200f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(560f, 160f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(600f, 120f)
				horizontalLineToRelative(200f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(840f, 160f)
				verticalLineToRelative(200f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(800f, 400f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(760f, 360f)
				verticalLineToRelative(-104f)
				close()
			}
		}.build()

		return _External!!
	}

@Suppress("ObjectPropertyName")
private var _External: ImageVector? = null
