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

val Icons.Outlined.Copy: ImageVector
	get() {
		if (_Copy != null) {
			return _Copy!!
		}
		_Copy = ImageVector.Builder(
			name = "Outlined.Copy",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(360f, 720f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(280f, 640f)
				verticalLineToRelative(-480f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(360f, 80f)
				horizontalLineToRelative(360f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(800f, 160f)
				verticalLineToRelative(480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(720f, 720f)
				lineTo(360f, 720f)
				close()
				moveTo(360f, 640f)
				horizontalLineToRelative(360f)
				verticalLineToRelative(-480f)
				lineTo(360f, 160f)
				verticalLineToRelative(480f)
				close()
				moveTo(200f, 880f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(120f, 800f)
				verticalLineToRelative(-520f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(160f, 240f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(200f, 280f)
				verticalLineToRelative(520f)
				horizontalLineToRelative(400f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(640f, 840f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(600f, 880f)
				lineTo(200f, 880f)
				close()
				moveTo(360f, 640f)
				verticalLineToRelative(-480f)
				verticalLineToRelative(480f)
				close()
			}
		}.build()

		return _Copy!!
	}

@Suppress("ObjectPropertyName")
private var _Copy: ImageVector? = null
