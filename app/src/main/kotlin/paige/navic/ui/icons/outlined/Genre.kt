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

val Icons.Outlined.Genre: ImageVector
	get() {
		if (_Genre != null) {
			return _Genre!!
		}
		_Genre = ImageVector.Builder(
			name = "Outlined.Genre",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(485f, 685f)
				quadToRelative(35f, -35f, 35f, -85f)
				verticalLineToRelative(-280f)
				horizontalLineToRelative(80f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(640f, 280f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(600f, 240f)
				lineTo(500f, 240f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(460f, 280f)
				verticalLineToRelative(216f)
				quadToRelative(-14f, -8f, -29f, -12f)
				reflectiveQuadToRelative(-31f, -4f)
				quadToRelative(-50f, 0f, -85f, 35f)
				reflectiveQuadToRelative(-35f, 85f)
				quadToRelative(0f, 50f, 35f, 85f)
				reflectiveQuadToRelative(85f, 35f)
				quadToRelative(50f, 0f, 85f, -35f)
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
				moveTo(480f, 800f)
				quadToRelative(134f, 0f, 227f, -93f)
				reflectiveQuadToRelative(93f, -227f)
				quadToRelative(0f, -134f, -93f, -227f)
				reflectiveQuadToRelative(-227f, -93f)
				quadToRelative(-134f, 0f, -227f, 93f)
				reflectiveQuadToRelative(-93f, 227f)
				quadToRelative(0f, 134f, 93f, 227f)
				reflectiveQuadToRelative(227f, 93f)
				close()
				moveTo(480f, 480f)
				close()
			}
		}.build()

		return _Genre!!
	}

@Suppress("ObjectPropertyName")
private var _Genre: ImageVector? = null
