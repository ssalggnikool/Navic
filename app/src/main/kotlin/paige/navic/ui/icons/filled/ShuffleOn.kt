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

val Icons.Filled.ShuffleOn: ImageVector
	get() {
		if (_ShuffleOn != null) {
			return _ShuffleOn!!
		}
		_ShuffleOn = ImageVector.Builder(
			name = "Filled.ShuffleOn",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(120f, 920f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(40f, 840f)
				verticalLineToRelative(-720f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(120f, 40f)
				horizontalLineToRelative(720f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(920f, 120f)
				verticalLineToRelative(720f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(840f, 920f)
				lineTo(120f, 920f)
				close()
				moveTo(600f, 800f)
				horizontalLineToRelative(160f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(800f, 760f)
				verticalLineToRelative(-160f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(760f, 560f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(720f, 600f)
				verticalLineToRelative(62f)
				lineToRelative(-97f, -97f)
				quadToRelative(-12f, -12f, -28.5f, -12f)
				reflectiveQuadTo(566f, 565f)
				quadToRelative(-12f, 12f, -12.5f, 28f)
				reflectiveQuadToRelative(11.5f, 28f)
				lineToRelative(99f, 99f)
				horizontalLineToRelative(-64f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(560f, 760f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(600f, 800f)
				close()
				moveTo(172f, 788f)
				quadToRelative(11f, 11f, 28f, 11f)
				reflectiveQuadToRelative(28f, -11f)
				lineToRelative(492f, -492f)
				verticalLineToRelative(64f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(760f, 400f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(800f, 360f)
				verticalLineToRelative(-160f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(760f, 160f)
				lineTo(600f, 160f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(560f, 200f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(600f, 240f)
				horizontalLineToRelative(64f)
				lineTo(172f, 732f)
				quadToRelative(-11f, 11f, -11f, 28f)
				reflectiveQuadToRelative(11f, 28f)
				close()
				moveTo(171f, 228f)
				lineTo(339f, 395f)
				quadToRelative(11f, 11f, 28f, 11f)
				reflectiveQuadToRelative(28f, -11f)
				quadToRelative(12f, -12f, 11.5f, -28.5f)
				reflectiveQuadTo(395f, 339f)
				lineTo(227f, 172f)
				quadToRelative(-12f, -11f, -28.5f, -11f)
				reflectiveQuadTo(171f, 172f)
				quadToRelative(-11f, 11f, -11f, 28f)
				reflectiveQuadToRelative(11f, 28f)
				close()
			}
		}.build()

		return _ShuffleOn!!
	}

@Suppress("ObjectPropertyName")
private var _ShuffleOn: ImageVector? = null
