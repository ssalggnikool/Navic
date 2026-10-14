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

val Icons.Outlined.QueuePlayNext: ImageVector
	get() {
		if (_QueuePlayNext != null) {
			return _QueuePlayNext!!
		}
		_QueuePlayNext = ImageVector.Builder(
			name = "Outlined.QueuePlayNext",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(13.3f, 20.275f)
				quadToRelative(-0.3f, -0.3f, -0.3f, -0.7f)
				reflectiveQuadToRelative(0.3f, -0.7f)
				lineTo(16.175f, 16f)
				horizontalLineTo(7f)
				quadToRelative(-0.825f, 0f, -1.412f, -0.587f)
				reflectiveQuadTo(5f, 14f)
				verticalLineTo(5f)
				quadToRelative(0f, -0.425f, 0.288f, -0.712f)
				reflectiveQuadTo(6f, 4f)
				reflectiveQuadToRelative(0.713f, 0.288f)
				reflectiveQuadTo(7f, 5f)
				verticalLineToRelative(9f)
				horizontalLineToRelative(9.175f)
				lineToRelative(-2.9f, -2.9f)
				quadToRelative(-0.3f, -0.3f, -0.288f, -0.7f)
				reflectiveQuadToRelative(0.288f, -0.7f)
				quadToRelative(0.3f, -0.3f, 0.7f, -0.312f)
				reflectiveQuadToRelative(0.7f, 0.287f)
				lineTo(19.3f, 14.3f)
				quadToRelative(0.15f, 0.15f, 0.212f, 0.325f)
				reflectiveQuadToRelative(0.063f, 0.375f)
				reflectiveQuadToRelative(-0.063f, 0.375f)
				reflectiveQuadToRelative(-0.212f, 0.325f)
				lineToRelative(-4.575f, 4.575f)
				quadToRelative(-0.3f, 0.3f, -0.712f, 0.3f)
				reflectiveQuadToRelative(-0.713f, -0.3f)
			}
		}.build()

		return _QueuePlayNext!!
	}

@Suppress("ObjectPropertyName")
private var _QueuePlayNext: ImageVector? = null
