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

val Icons.Outlined.Picker: ImageVector
	get() {
		if (_Picker != null) {
			return _Picker!!
		}
		_Picker = ImageVector.Builder(
			name = "Outlined.Picker",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(17.66f, 5.41f)
				lineToRelative(0.92f, 0.92f)
				lineToRelative(-2.69f, 2.69f)
				lineToRelative(-0.92f, -0.92f)
				lineToRelative(2.69f, -2.69f)
				moveTo(17.67f, 3f)
				curveToRelative(-0.26f, 0f, -0.51f, 0.1f, -0.71f, 0.29f)
				lineToRelative(-3.12f, 3.12f)
				lineToRelative(-1.93f, -1.91f)
				lineToRelative(-1.41f, 1.41f)
				lineToRelative(1.42f, 1.42f)
				lineTo(3f, 16.25f)
				verticalLineTo(21f)
				horizontalLineToRelative(4.75f)
				lineToRelative(8.92f, -8.92f)
				lineToRelative(1.42f, 1.42f)
				lineToRelative(1.41f, -1.41f)
				lineToRelative(-1.92f, -1.92f)
				lineToRelative(3.12f, -3.12f)
				curveToRelative(0.4f, -0.4f, 0.4f, -1.03f, 0.01f, -1.42f)
				lineToRelative(-2.34f, -2.34f)
				curveToRelative(-0.2f, -0.19f, -0.45f, -0.29f, -0.7f, -0.29f)
				close()
				moveTo(6.92f, 19f)
				lineTo(5f, 17.08f)
				lineToRelative(8.06f, -8.06f)
				lineToRelative(1.92f, 1.92f)
				lineTo(6.92f, 19f)
				close()
			}
		}.build()

		return _Picker!!
	}

@Suppress("ObjectPropertyName")
private var _Picker: ImageVector? = null
