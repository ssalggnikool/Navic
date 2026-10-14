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

val Icons.Outlined.Radio: ImageVector
	get() {
		if (_Radio != null) {
			return _Radio!!
		}
		_Radio = ImageVector.Builder(
			name = "Outlined.Radio",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(20f, 6f)
				lineTo(8.3f, 6f)
				lineToRelative(8.26f, -3.34f)
				lineTo(15.88f, 1f)
				lineTo(3.24f, 6.15f)
				curveTo(2.51f, 6.43f, 2f, 7.17f, 2f, 8f)
				verticalLineToRelative(12f)
				curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
				horizontalLineToRelative(16f)
				curveToRelative(1.11f, 0f, 2f, -0.9f, 2f, -2f)
				lineTo(22f, 8f)
				curveToRelative(0f, -1.11f, -0.89f, -2f, -2f, -2f)
				close()
				moveTo(20f, 8f)
				verticalLineToRelative(3f)
				horizontalLineToRelative(-2f)
				lineTo(18f, 9f)
				horizontalLineToRelative(-2f)
				verticalLineToRelative(2f)
				lineTo(4f, 11f)
				lineTo(4f, 8f)
				horizontalLineToRelative(16f)
				close()
				moveTo(4f, 20f)
				verticalLineToRelative(-7f)
				horizontalLineToRelative(16f)
				verticalLineToRelative(7f)
				lineTo(4f, 20f)
				close()
			}
			path(fill = SolidColor(Color.White)) {
				moveTo(8f, 16.48f)
				moveToRelative(-2.5f, 0f)
				arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 0f)
				arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, 0f)
			}
		}.build()

		return _Radio!!
	}

@Suppress("ObjectPropertyName")
private var _Radio: ImageVector? = null
