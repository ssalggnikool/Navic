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

val Icons.Outlined.Search: ImageVector
	get() {
		if (_Search != null) {
			return _Search!!
		}
		_Search = ImageVector.Builder(
			name = "Outlined.Search",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(380f, 640f)
				quadToRelative(-109f, 0f, -184.5f, -75.5f)
				reflectiveQuadTo(120f, 380f)
				quadToRelative(0f, -109f, 75.5f, -184.5f)
				reflectiveQuadTo(380f, 120f)
				quadToRelative(109f, 0f, 184.5f, 75.5f)
				reflectiveQuadTo(640f, 380f)
				quadToRelative(0f, 44f, -14f, 83f)
				reflectiveQuadToRelative(-38f, 69f)
				lineToRelative(224f, 224f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				quadToRelative(-11f, 11f, -28f, 11f)
				reflectiveQuadToRelative(-28f, -11f)
				lineTo(532f, 588f)
				quadToRelative(-30f, 24f, -69f, 38f)
				reflectiveQuadToRelative(-83f, 14f)
				close()
				moveTo(380f, 560f)
				quadToRelative(75f, 0f, 127.5f, -52.5f)
				reflectiveQuadTo(560f, 380f)
				quadToRelative(0f, -75f, -52.5f, -127.5f)
				reflectiveQuadTo(380f, 200f)
				quadToRelative(-75f, 0f, -127.5f, 52.5f)
				reflectiveQuadTo(200f, 380f)
				quadToRelative(0f, 75f, 52.5f, 127.5f)
				reflectiveQuadTo(380f, 560f)
				close()
			}
		}.build()

		return _Search!!
	}

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
