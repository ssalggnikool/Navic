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

val Icons.Outlined.Artist: ImageVector
	get() {
		if (_Artist != null) {
			return _Artist!!
		}
		_Artist = ImageVector.Builder(
			name = "Outlined.Artist",
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
				moveTo(160f, 800f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 760f)
				verticalLineToRelative(-72f)
				quadToRelative(0f, -35f, 17.5f, -63f)
				reflectiveQuadToRelative(46.5f, -43f)
				quadToRelative(62f, -31f, 126f, -46.5f)
				reflectiveQuadTo(440f, 520f)
				quadToRelative(23f, 0f, 47f, 1.5f)
				reflectiveQuadToRelative(47f, 6.5f)
				quadToRelative(21f, 4f, 29f, 18f)
				reflectiveQuadToRelative(6f, 29f)
				quadToRelative(-2f, 15f, -14f, 26f)
				reflectiveQuadToRelative(-31f, 7f)
				quadToRelative(-21f, -4f, -41.5f, -6f)
				reflectiveQuadToRelative(-42.5f, -2f)
				quadToRelative(-57f, 0f, -112f, 14f)
				reflectiveQuadToRelative(-108f, 40f)
				quadToRelative(-9f, 5f, -14.5f, 14f)
				reflectiveQuadToRelative(-5.5f, 20f)
				verticalLineToRelative(32f)
				horizontalLineToRelative(293f)
				quadToRelative(20f, 0f, 30f, 12.5f)
				reflectiveQuadToRelative(10f, 27.5f)
				quadToRelative(0f, 15f, -10f, 27.5f)
				reflectiveQuadTo(493f, 800f)
				lineTo(160f, 800f)
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
				moveTo(496.5f, 376.5f)
				quadTo(520f, 353f, 520f, 320f)
				reflectiveQuadToRelative(-23.5f, -56.5f)
				quadTo(473f, 240f, 440f, 240f)
				reflectiveQuadToRelative(-56.5f, 23.5f)
				quadTo(360f, 287f, 360f, 320f)
				reflectiveQuadToRelative(23.5f, 56.5f)
				quadTo(407f, 400f, 440f, 400f)
				reflectiveQuadToRelative(56.5f, -23.5f)
				close()
				moveTo(440f, 320f)
				close()
				moveTo(440f, 720f)
				close()
			}
		}.build()

		return _Artist!!
	}

@Suppress("ObjectPropertyName")
private var _Artist: ImageVector? = null
