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

val Icons.Filled.Radio: ImageVector
	get() {
		if (_Radio != null) {
			return _Radio!!
		}
		_Radio = ImageVector.Builder(
			name = "Filled.Radio",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(160f, 880f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(80f, 800f)
				verticalLineToRelative(-534f)
				lineToRelative(556f, -226f)
				lineToRelative(26f, 66f)
				lineToRelative(-330f, 134f)
				horizontalLineToRelative(468f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(880f, 320f)
				verticalLineToRelative(480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(800f, 880f)
				lineTo(160f, 880f)
				close()
				moveTo(391f, 731f)
				quadToRelative(29f, -29f, 29f, -71f)
				reflectiveQuadToRelative(-29f, -71f)
				quadToRelative(-29f, -29f, -71f, -29f)
				reflectiveQuadToRelative(-71f, 29f)
				quadToRelative(-29f, 29f, -29f, 71f)
				reflectiveQuadToRelative(29f, 71f)
				quadToRelative(29f, 29f, 71f, 29f)
				reflectiveQuadToRelative(71f, -29f)
				close()
				moveTo(160f, 440f)
				horizontalLineToRelative(480f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(-120f)
				lineTo(160f, 320f)
				verticalLineToRelative(120f)
				close()
			}
		}.build()

		return _Radio!!
	}

@Suppress("ObjectPropertyName")
private var _Radio: ImageVector? = null
