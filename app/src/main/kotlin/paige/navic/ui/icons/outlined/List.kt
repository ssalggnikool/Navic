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

val Icons.Outlined.List: ImageVector
	get() {
		if (_List != null) {
			return _List!!
		}
		_List = ImageVector.Builder(
			name = "Outlined.List",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(320f, 360f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(280f, 320f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(320f, 280f)
				horizontalLineToRelative(480f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(840f, 320f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(800f, 360f)
				lineTo(320f, 360f)
				close()
				moveTo(320f, 520f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(280f, 480f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(320f, 440f)
				horizontalLineToRelative(480f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(840f, 480f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(800f, 520f)
				lineTo(320f, 520f)
				close()
				moveTo(320f, 680f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(280f, 640f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(320f, 600f)
				horizontalLineToRelative(480f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(840f, 640f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(800f, 680f)
				lineTo(320f, 680f)
				close()
				moveTo(160f, 360f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 320f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 280f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(200f, 320f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(160f, 360f)
				close()
				moveTo(160f, 520f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 480f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 440f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(200f, 480f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(160f, 520f)
				close()
				moveTo(160f, 680f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(120f, 640f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 600f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(200f, 640f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(160f, 680f)
				close()
			}
		}.build()

		return _List!!
	}

@Suppress("ObjectPropertyName")
private var _List: ImageVector? = null
