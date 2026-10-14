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

val Icons.Outlined.Lyrics: ImageVector
	get() {
		if (_Lyrics != null) {
			return _Lyrics!!
		}
		_Lyrics = ImageVector.Builder(
			name = "Outlined.Lyrics",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(280f, 560f)
				horizontalLineToRelative(80f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(400f, 520f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(360f, 480f)
				horizontalLineToRelative(-80f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(240f, 520f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(280f, 560f)
				close()
				moveTo(760f, 480f)
				quadToRelative(-50f, 0f, -85f, -35f)
				reflectiveQuadToRelative(-35f, -85f)
				quadToRelative(0f, -50f, 35f, -85f)
				reflectiveQuadToRelative(85f, -35f)
				quadToRelative(11f, 0f, 21f, 2f)
				reflectiveQuadToRelative(19f, 5f)
				verticalLineToRelative(-167f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(840f, 40f)
				horizontalLineToRelative(80f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(960f, 80f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(920f, 120f)
				horizontalLineToRelative(-40f)
				verticalLineToRelative(240f)
				quadToRelative(0f, 50f, -35f, 85f)
				reflectiveQuadToRelative(-85f, 35f)
				close()
				moveTo(280f, 440f)
				horizontalLineToRelative(200f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(520f, 400f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(480f, 360f)
				lineTo(280f, 360f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(240f, 400f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(280f, 440f)
				close()
				moveTo(280f, 320f)
				horizontalLineToRelative(200f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(520f, 280f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(480f, 240f)
				lineTo(280f, 240f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(240f, 280f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(280f, 320f)
				close()
				moveTo(600f, 720f)
				lineTo(240f, 720f)
				lineToRelative(-92f, 92f)
				quadToRelative(-6f, 6f, -13f, 9f)
				reflectiveQuadToRelative(-15f, 3f)
				quadToRelative(-16f, 0f, -28f, -11.5f)
				reflectiveQuadTo(80f, 783f)
				verticalLineToRelative(-623f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(160f, 80f)
				horizontalLineToRelative(440f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(680f, 160f)
				verticalLineToRelative(17f)
				quadToRelative(-24f, 11f, -44f, 27f)
				reflectiveQuadToRelative(-36f, 36f)
				verticalLineToRelative(-80f)
				lineTo(160f, 160f)
				verticalLineToRelative(527f)
				lineToRelative(47f, -47f)
				horizontalLineToRelative(393f)
				verticalLineToRelative(-160f)
				quadToRelative(16f, 20f, 36f, 36f)
				reflectiveQuadToRelative(44f, 27f)
				verticalLineToRelative(97f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(600f, 720f)
				close()
				moveTo(160f, 640f)
				verticalLineToRelative(-480f)
				verticalLineToRelative(480f)
				close()
			}
		}.build()

		return _Lyrics!!
	}

@Suppress("ObjectPropertyName")
private var _Lyrics: ImageVector? = null
