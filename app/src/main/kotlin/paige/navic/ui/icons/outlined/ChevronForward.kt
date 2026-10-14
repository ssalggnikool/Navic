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

val Icons.Outlined.ChevronForward: ImageVector
	get() {
		if (_ChevronForward != null) {
			return _ChevronForward!!
		}
		_ChevronForward = ImageVector.Builder(
			name = "Outlined.ChevronForward",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(504f, 480f)
				lineTo(348f, 324f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(28f, 11f)
				lineToRelative(184f, 184f)
				quadToRelative(6f, 6f, 8.5f, 13f)
				reflectiveQuadToRelative(2.5f, 15f)
				quadToRelative(0f, 8f, -2.5f, 15f)
				reflectiveQuadToRelative(-8.5f, 13f)
				lineTo(404f, 692f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(11f, -28f)
				lineToRelative(156f, -156f)
				close()
			}
		}.build()

		return _ChevronForward!!
	}

@Suppress("ObjectPropertyName")
private var _ChevronForward: ImageVector? = null
