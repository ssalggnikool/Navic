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

val Icons.Outlined.Speed: ImageVector
	get() {
		if (_Speed != null) {
			return _Speed!!
		}
		_Speed = ImageVector.Builder(
			name = "Outlined.Speed",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(480f, 643.5f)
				quadToRelative(38f, -0.5f, 56f, -27.5f)
				lineToRelative(224f, -336f)
				lineToRelative(-336f, 224f)
				quadToRelative(-27f, 18f, -28.5f, 55f)
				reflectiveQuadToRelative(22.5f, 61f)
				quadToRelative(24f, 24f, 62f, 23.5f)
				close()
				moveTo(480f, 160f)
				quadToRelative(59f, 0f, 113.5f, 16.5f)
				reflectiveQuadTo(696f, 226f)
				lineToRelative(-76f, 48f)
				quadToRelative(-33f, -17f, -68.5f, -25.5f)
				reflectiveQuadTo(480f, 240f)
				quadToRelative(-133f, 0f, -226.5f, 93.5f)
				reflectiveQuadTo(160f, 560f)
				quadToRelative(0f, 42f, 11.5f, 83f)
				reflectiveQuadToRelative(32.5f, 77f)
				horizontalLineToRelative(552f)
				quadToRelative(23f, -38f, 33.5f, -79f)
				reflectiveQuadToRelative(10.5f, -85f)
				quadToRelative(0f, -36f, -8.5f, -70f)
				reflectiveQuadTo(766f, 420f)
				lineToRelative(48f, -76f)
				quadToRelative(30f, 47f, 47.5f, 100f)
				reflectiveQuadTo(880f, 554f)
				quadToRelative(1f, 57f, -13f, 109f)
				reflectiveQuadToRelative(-41f, 99f)
				quadToRelative(-11f, 18f, -30f, 28f)
				reflectiveQuadToRelative(-40f, 10f)
				lineTo(204f, 800f)
				quadToRelative(-21f, 0f, -40f, -10f)
				reflectiveQuadToRelative(-30f, -28f)
				quadToRelative(-26f, -45f, -40f, -95.5f)
				reflectiveQuadTo(80f, 560f)
				quadToRelative(0f, -83f, 31.5f, -155.5f)
				reflectiveQuadToRelative(86f, -127f)
				quadTo(252f, 223f, 325f, 191.5f)
				reflectiveQuadTo(480f, 160f)
				close()
				moveTo(487f, 473f)
				close()
			}
		}.build()

		return _Speed!!
	}

@Suppress("ObjectPropertyName")
private var _Speed: ImageVector? = null
