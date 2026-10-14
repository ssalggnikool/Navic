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

val Icons.Filled.Artist: ImageVector
	get() {
		if (_Artist != null) {
			return _Artist!!
		}
		_Artist = ImageVector.Builder(
			name = "Filled.Artist",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(629f, 771f)
				quadToRelative(-29f, -29f, -29f, -71f)
				reflectiveQuadToRelative(29f, -71f)
				quadToRelative(29f, -29f, 71f, -29f)
				quadToRelative(8f, 0f, 18f, 1.5f)
				reflectiveQuadToRelative(22f, 6.5f)
				verticalLineToRelative(-168f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(780f, 400f)
				horizontalLineToRelative(60f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(880f, 440f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(840f, 480f)
				horizontalLineToRelative(-40f)
				verticalLineToRelative(220f)
				quadToRelative(0f, 42f, -29f, 71f)
				reflectiveQuadToRelative(-71f, 29f)
				quadToRelative(-42f, 0f, -71f, -29f)
				close()
				moveTo(327f, 433f)
				quadToRelative(-47f, -47f, -47f, -113f)
				reflectiveQuadToRelative(47f, -113f)
				quadToRelative(47f, -47f, 113f, -47f)
				reflectiveQuadToRelative(113f, 47f)
				quadToRelative(47f, 47f, 47f, 113f)
				reflectiveQuadToRelative(-47f, 113f)
				quadToRelative(-47f, 47f, -113f, 47f)
				reflectiveQuadToRelative(-113f, -47f)
				close()
				moveTo(160f, 800f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 760f)
				verticalLineToRelative(-72f)
				quadToRelative(0f, -35f, 17.5f, -63f)
				reflectiveQuadToRelative(46.5f, -43f)
				quadToRelative(62f, -31f, 126f, -46.5f)
				reflectiveQuadTo(440f, 520f)
				quadToRelative(28f, 0f, 55.5f, 3f)
				reflectiveQuadToRelative(55.5f, 9f)
				quadToRelative(17f, 4f, 21.5f, 21f)
				reflectiveQuadToRelative(-9.5f, 31f)
				quadToRelative(-21f, 25f, -31.5f, 54.5f)
				reflectiveQuadTo(521f, 700f)
				quadToRelative(0f, 13f, 1.5f, 25.5f)
				reflectiveQuadTo(528f, 751f)
				quadToRelative(5f, 18f, -4.5f, 33.5f)
				reflectiveQuadTo(497f, 800f)
				lineTo(160f, 800f)
				close()
			}
		}.build()

		return _Artist!!
	}

@Suppress("ObjectPropertyName")
private var _Artist: ImageVector? = null
