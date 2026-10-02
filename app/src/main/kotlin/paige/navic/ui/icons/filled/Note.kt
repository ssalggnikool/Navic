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

val Icons.Filled.Note: ImageVector
	get() {
		if (_Note != null) {
			return _Note!!
		}
		_Note = ImageVector.Builder(
			name = "Filled.Note",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(127f, 793f)
				quadToRelative(-47f, -47f, -47f, -113f)
				reflectiveQuadToRelative(47f, -113f)
				quadToRelative(47f, -47f, 113f, -47f)
				quadToRelative(23f, 0f, 42.5f, 5.5f)
				reflectiveQuadTo(320f, 542f)
				verticalLineToRelative(-342f)
				lineToRelative(480f, -80f)
				verticalLineToRelative(480f)
				quadToRelative(0f, 66f, -47f, 113f)
				reflectiveQuadToRelative(-113f, 47f)
				quadToRelative(-66f, 0f, -113f, -47f)
				reflectiveQuadToRelative(-47f, -113f)
				quadToRelative(0f, -66f, 47f, -113f)
				reflectiveQuadToRelative(113f, -47f)
				quadToRelative(23f, 0f, 42.5f, 5.5f)
				reflectiveQuadTo(720f, 462f)
				verticalLineToRelative(-165f)
				lineToRelative(-320f, 63f)
				verticalLineToRelative(320f)
				quadToRelative(0f, 66f, -47f, 113f)
				reflectiveQuadToRelative(-113f, 47f)
				quadToRelative(-66f, 0f, -113f, -47f)
				close()
			}
		}.build()

		return _Note!!
	}

@Suppress("ObjectPropertyName")
private var _Note: ImageVector? = null
