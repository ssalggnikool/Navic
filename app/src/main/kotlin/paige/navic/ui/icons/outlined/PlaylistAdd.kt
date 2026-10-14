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

val Icons.Outlined.PlaylistAdd: ImageVector
	get() {
		if (_PlaylistAdd != null) {
			return _PlaylistAdd!!
		}
		_PlaylistAdd = ImageVector.Builder(
			name = "Outlined.PlaylistAdd",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(160f, 640f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 600f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 560f)
				horizontalLineToRelative(200f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(400f, 600f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(360f, 640f)
				lineTo(160f, 640f)
				close()
				moveTo(160f, 480f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 440f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 400f)
				horizontalLineToRelative(360f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(560f, 440f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(520f, 480f)
				lineTo(160f, 480f)
				close()
				moveTo(160f, 320f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 280f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 240f)
				horizontalLineToRelative(360f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(560f, 280f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(520f, 320f)
				lineTo(160f, 320f)
				close()
				moveTo(651.5f, 788.5f)
				quadTo(640f, 777f, 640f, 760f)
				verticalLineToRelative(-120f)
				lineTo(520f, 640f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(480f, 600f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(520f, 560f)
				horizontalLineToRelative(120f)
				verticalLineToRelative(-120f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(680f, 400f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(720f, 440f)
				verticalLineToRelative(120f)
				horizontalLineToRelative(120f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(880f, 600f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(840f, 640f)
				lineTo(720f, 640f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(680f, 800f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				close()
			}
		}.build()

		return _PlaylistAdd!!
	}

@Suppress("ObjectPropertyName")
private var _PlaylistAdd: ImageVector? = null
