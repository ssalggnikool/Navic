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

val Icons.Filled.Album: ImageVector
	get() {
		if (_Album != null) {
			return _Album!!
		}
		_Album = ImageVector.Builder(
			name = "Filled.Album",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(480f, 660f)
				quadToRelative(75f, 0f, 127.5f, -52.5f)
				reflectiveQuadTo(660f, 480f)
				quadToRelative(0f, -75f, -52.5f, -127.5f)
				reflectiveQuadTo(480f, 300f)
				quadToRelative(-75f, 0f, -127.5f, 52.5f)
				reflectiveQuadTo(300f, 480f)
				quadToRelative(0f, 75f, 52.5f, 127.5f)
				reflectiveQuadTo(480f, 660f)
				close()
				moveTo(451.5f, 508.5f)
				quadTo(440f, 497f, 440f, 480f)
				reflectiveQuadToRelative(11.5f, -28.5f)
				quadTo(463f, 440f, 480f, 440f)
				reflectiveQuadToRelative(28.5f, 11.5f)
				quadTo(520f, 463f, 520f, 480f)
				reflectiveQuadToRelative(-11.5f, 28.5f)
				quadTo(497f, 520f, 480f, 520f)
				reflectiveQuadToRelative(-28.5f, -11.5f)
				close()
				moveTo(480f, 880f)
				quadToRelative(-83f, 0f, -156f, -31.5f)
				reflectiveQuadTo(197f, 763f)
				quadToRelative(-54f, -54f, -85.5f, -127f)
				reflectiveQuadTo(80f, 480f)
				quadToRelative(0f, -83f, 31.5f, -156f)
				reflectiveQuadTo(197f, 197f)
				quadToRelative(54f, -54f, 127f, -85.5f)
				reflectiveQuadTo(480f, 80f)
				quadToRelative(83f, 0f, 156f, 31.5f)
				reflectiveQuadTo(763f, 197f)
				quadToRelative(54f, 54f, 85.5f, 127f)
				reflectiveQuadTo(880f, 480f)
				quadToRelative(0f, 83f, -31.5f, 156f)
				reflectiveQuadTo(763f, 763f)
				quadToRelative(-54f, 54f, -127f, 85.5f)
				reflectiveQuadTo(480f, 880f)
				close()
			}
		}.build()

		return _Album!!
	}

@Suppress("ObjectPropertyName")
private var _Album: ImageVector? = null
