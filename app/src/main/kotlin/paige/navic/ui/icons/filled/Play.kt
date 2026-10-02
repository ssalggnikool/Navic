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

val Icons.Filled.Play: ImageVector
	get() {
		if (_Play != null) {
			return _Play!!
		}
		_Play = ImageVector.Builder(
			name = "Filled.Play",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(320f, 687f)
				verticalLineToRelative(-414f)
				quadToRelative(0f, -17f, 12f, -28.5f)
				reflectiveQuadToRelative(28f, -11.5f)
				quadToRelative(5f, 0f, 10.5f, 1.5f)
				reflectiveQuadTo(381f, 239f)
				lineToRelative(326f, 207f)
				quadToRelative(9f, 6f, 13.5f, 15f)
				reflectiveQuadToRelative(4.5f, 19f)
				quadToRelative(0f, 10f, -4.5f, 19f)
				reflectiveQuadTo(707f, 514f)
				lineTo(381f, 721f)
				quadToRelative(-5f, 3f, -10.5f, 4.5f)
				reflectiveQuadTo(360f, 727f)
				quadToRelative(-16f, 0f, -28f, -11.5f)
				reflectiveQuadTo(320f, 687f)
				close()
			}
		}.build()

		return _Play!!
	}

@Suppress("ObjectPropertyName")
private var _Play: ImageVector? = null
