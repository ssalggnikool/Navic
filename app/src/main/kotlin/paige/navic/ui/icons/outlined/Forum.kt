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

val Icons.Outlined.Forum: ImageVector
	get() {
		if (_Forum != null) {
			return _Forum!!
		}
		_Forum = ImageVector.Builder(
			name = "Outlined.Forum",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(840f, 824f)
				quadToRelative(-8f, 0f, -15f, -3f)
				reflectiveQuadToRelative(-13f, -9f)
				lineToRelative(-92f, -92f)
				lineTo(320f, 720f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(240f, 640f)
				verticalLineToRelative(-40f)
				horizontalLineToRelative(440f)
				quadToRelative(33f, 0f, 56.5f, -23.5f)
				reflectiveQuadTo(760f, 520f)
				verticalLineToRelative(-280f)
				horizontalLineToRelative(40f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(880f, 320f)
				verticalLineToRelative(463f)
				quadToRelative(0f, 18f, -12f, 29.5f)
				reflectiveQuadTo(840f, 824f)
				close()
				moveTo(160f, 487f)
				lineToRelative(47f, -47f)
				horizontalLineToRelative(393f)
				verticalLineToRelative(-280f)
				lineTo(160f, 160f)
				verticalLineToRelative(327f)
				close()
				moveTo(120f, 624f)
				quadToRelative(-16f, 0f, -28f, -11.5f)
				reflectiveQuadTo(80f, 583f)
				verticalLineToRelative(-423f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(160f, 80f)
				horizontalLineToRelative(440f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(680f, 160f)
				verticalLineToRelative(280f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(600f, 520f)
				lineTo(240f, 520f)
				lineToRelative(-92f, 92f)
				quadToRelative(-6f, 6f, -13f, 9f)
				reflectiveQuadToRelative(-15f, 3f)
				close()
				moveTo(160f, 440f)
				verticalLineToRelative(-280f)
				verticalLineToRelative(280f)
				close()
			}
		}.build()

		return _Forum!!
	}

@Suppress("ObjectPropertyName")
private var _Forum: ImageVector? = null
