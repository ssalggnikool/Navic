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

val Icons.Outlined.Add: ImageVector
	get() {
		if (_Add != null) {
			return _Add!!
		}
		_Add = ImageVector.Builder(
			name = "Outlined.Add",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(440f, 520f)
				lineTo(240f, 520f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(200f, 480f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(240f, 440f)
				horizontalLineToRelative(200f)
				verticalLineToRelative(-200f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(480f, 200f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(520f, 240f)
				verticalLineToRelative(200f)
				horizontalLineToRelative(200f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(760f, 480f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(720f, 520f)
				lineTo(520f, 520f)
				verticalLineToRelative(200f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(480f, 760f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(440f, 720f)
				verticalLineToRelative(-200f)
				close()
			}
		}.build()

		return _Add!!
	}

@Suppress("ObjectPropertyName")
private var _Add: ImageVector? = null
