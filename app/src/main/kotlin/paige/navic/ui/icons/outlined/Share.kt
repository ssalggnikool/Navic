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

val Icons.Outlined.Share: ImageVector
	get() {
		if (_Share != null) {
			return _Share!!
		}
		_Share = ImageVector.Builder(
			name = "Outlined.Share",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(680f, 880f)
				quadToRelative(-50f, 0f, -85f, -35f)
				reflectiveQuadToRelative(-35f, -85f)
				quadToRelative(0f, -6f, 3f, -28f)
				lineTo(282f, 568f)
				quadToRelative(-16f, 15f, -37f, 23.5f)
				reflectiveQuadToRelative(-45f, 8.5f)
				quadToRelative(-50f, 0f, -85f, -35f)
				reflectiveQuadToRelative(-35f, -85f)
				quadToRelative(0f, -50f, 35f, -85f)
				reflectiveQuadToRelative(85f, -35f)
				quadToRelative(24f, 0f, 45f, 8.5f)
				reflectiveQuadToRelative(37f, 23.5f)
				lineToRelative(281f, -164f)
				quadToRelative(-2f, -7f, -2.5f, -13.5f)
				reflectiveQuadTo(560f, 200f)
				quadToRelative(0f, -50f, 35f, -85f)
				reflectiveQuadToRelative(85f, -35f)
				quadToRelative(50f, 0f, 85f, 35f)
				reflectiveQuadToRelative(35f, 85f)
				quadToRelative(0f, 50f, -35f, 85f)
				reflectiveQuadToRelative(-85f, 35f)
				quadToRelative(-24f, 0f, -45f, -8.5f)
				reflectiveQuadTo(598f, 288f)
				lineTo(317f, 452f)
				quadToRelative(2f, 7f, 2.5f, 13.5f)
				reflectiveQuadToRelative(0.5f, 14.5f)
				quadToRelative(0f, 8f, -0.5f, 14.5f)
				reflectiveQuadTo(317f, 508f)
				lineToRelative(281f, 164f)
				quadToRelative(16f, -15f, 37f, -23.5f)
				reflectiveQuadToRelative(45f, -8.5f)
				quadToRelative(50f, 0f, 85f, 35f)
				reflectiveQuadToRelative(35f, 85f)
				quadToRelative(0f, 50f, -35f, 85f)
				reflectiveQuadToRelative(-85f, 35f)
				close()
			}
		}.build()

		return _Share!!
	}

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null
