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

val Icons.Outlined.Check: ImageVector
	get() {
		if (_Check != null) {
			return _Check!!
		}
		_Check = ImageVector.Builder(
			name = "Outlined.Check",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(382f, 606f)
				lineToRelative(339f, -339f)
				quadToRelative(12f, -12f, 28f, -12f)
				reflectiveQuadToRelative(28f, 12f)
				quadToRelative(12f, 12f, 12f, 28.5f)
				reflectiveQuadTo(777f, 324f)
				lineTo(410f, 692f)
				quadToRelative(-12f, 12f, -28f, 12f)
				reflectiveQuadToRelative(-28f, -12f)
				lineTo(182f, 520f)
				quadToRelative(-12f, -12f, -11.5f, -28.5f)
				reflectiveQuadTo(183f, 463f)
				quadToRelative(12f, -12f, 28.5f, -12f)
				reflectiveQuadToRelative(28.5f, 12f)
				lineToRelative(142f, 143f)
				close()
			}
		}.build()

		return _Check!!
	}

@Suppress("ObjectPropertyName")
private var _Check: ImageVector? = null
