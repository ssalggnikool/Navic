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

val Icons.Outlined.LibraryAdd: ImageVector
	get() {
		if (_LibraryAdd != null) {
			return _LibraryAdd!!
		}
		_LibraryAdd = ImageVector.Builder(
			name = "Outlined.LibraryAdd",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(588.5f, 548.5f)
				quadTo(600f, 537f, 600f, 520f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(80f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(720f, 400f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(680f, 360f)
				horizontalLineToRelative(-80f)
				verticalLineToRelative(-80f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(560f, 240f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(520f, 280f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(-80f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(400f, 400f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(440f, 440f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(80f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(560f, 560f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				close()
				moveTo(320f, 720f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(240f, 640f)
				verticalLineToRelative(-480f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(320f, 80f)
				horizontalLineToRelative(480f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(880f, 160f)
				verticalLineToRelative(480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(800f, 720f)
				lineTo(320f, 720f)
				close()
				moveTo(320f, 640f)
				horizontalLineToRelative(480f)
				verticalLineToRelative(-480f)
				lineTo(320f, 160f)
				verticalLineToRelative(480f)
				close()
				moveTo(160f, 880f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(80f, 800f)
				verticalLineToRelative(-520f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(120f, 240f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(160f, 280f)
				verticalLineToRelative(520f)
				horizontalLineToRelative(520f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(720f, 840f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(680f, 880f)
				lineTo(160f, 880f)
				close()
				moveTo(320f, 160f)
				verticalLineToRelative(480f)
				verticalLineToRelative(-480f)
				close()
			}
		}.build()

		return _LibraryAdd!!
	}

@Suppress("ObjectPropertyName")
private var _LibraryAdd: ImageVector? = null
