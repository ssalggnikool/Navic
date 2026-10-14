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

val Icons.Outlined.Shuffle: ImageVector
	get() {
		if (_Shuffle != null) {
			return _Shuffle!!
		}
		_Shuffle = ImageVector.Builder(
			name = "Outlined.Shuffle",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(600f, 800f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(560f, 760f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(600f, 720f)
				horizontalLineToRelative(64f)
				lineToRelative(-99f, -99f)
				quadToRelative(-12f, -12f, -11.5f, -28.5f)
				reflectiveQuadTo(566f, 564f)
				quadToRelative(12f, -12f, 28.5f, -12f)
				reflectiveQuadToRelative(28.5f, 12f)
				lineToRelative(97f, 98f)
				verticalLineToRelative(-62f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(760f, 560f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(800f, 600f)
				verticalLineToRelative(160f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(760f, 800f)
				lineTo(600f, 800f)
				close()
				moveTo(172f, 788f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				lineToRelative(492f, -492f)
				horizontalLineToRelative(-64f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(560f, 200f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(600f, 160f)
				horizontalLineToRelative(160f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(800f, 200f)
				verticalLineToRelative(160f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(760f, 400f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(720f, 360f)
				verticalLineToRelative(-64f)
				lineTo(228f, 788f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				close()
				moveTo(171f, 228f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				quadToRelative(11f, -11f, 27.5f, -11f)
				reflectiveQuadToRelative(28.5f, 11f)
				lineToRelative(168f, 167f)
				quadToRelative(11f, 11f, 11.5f, 27.5f)
				reflectiveQuadTo(395f, 395f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				lineTo(171f, 228f)
				close()
			}
		}.build()

		return _Shuffle!!
	}

@Suppress("ObjectPropertyName")
private var _Shuffle: ImageVector? = null
