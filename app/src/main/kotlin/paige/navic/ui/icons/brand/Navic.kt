/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.icons.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons

val Icons.Brand.Navic: ImageVector
	get() {
		if (_Navic != null) {
			return _Navic!!
		}
		_Navic = ImageVector.Builder(
			name = "Brand.Navic",
			defaultWidth = 94.dp,
			defaultHeight = 94.dp,
			viewportWidth = 94f,
			viewportHeight = 94f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(39.01f, 71.56f)
				verticalLineToRelative(-34.2f)
				lineTo(64.14f, 60.2f)
				curveToRelative(1.43f, 1.3f, 1.54f, 3.52f, 0.24f, 4.95f)
				curveToRelative(-1.3f, 1.44f, -3.52f, 1.54f, -4.95f, 0.24f)
				lineTo(46.03f, 53.2f)
				verticalLineToRelative(18.36f)
				curveToRelative(0f, 1.93f, -1.58f, 3.5f, -3.51f, 3.5f)
				curveToRelative(-1.94f, 0f, -3.5f, -1.57f, -3.5f, -3.5f)
				close()
			}
			path(fill = SolidColor(Color.White)) {
				moveTo(35.51f, 82.06f)
				curveToRelative(5.8f, 0f, 10.5f, -4.7f, 10.5f, -10.5f)
				curveToRelative(0f, -5.81f, -4.7f, -10.51f, -10.5f, -10.51f)
				reflectiveCurveTo(25f, 65.75f, 25f, 71.55f)
				reflectiveCurveToRelative(4.7f, 10.51f, 10.51f, 10.51f)
				close()
			}
			path(fill = SolidColor(Color.White)) {
				moveTo(26.75f, 52.29f)
				verticalLineTo(19f)
				curveToRelative(0f, -1.36f, 0.79f, -2.6f, 2.02f, -3.18f)
				curveToRelative(1.23f, -0.57f, 2.68f, -0.39f, 3.73f, 0.48f)
				lineTo(58.28f, 37.8f)
				verticalLineTo(22.5f)
				curveToRelative(0f, -1.93f, 1.57f, -3.5f, 3.5f, -3.5f)
				curveToRelative(1.94f, 0f, 3.5f, 1.57f, 3.5f, 3.5f)
				verticalLineToRelative(22.78f)
				curveToRelative(0f, 1.36f, -0.78f, 2.6f, -2.01f, 3.17f)
				curveToRelative(-1.23f, 0.58f, -2.69f, 0.4f, -3.73f, -0.48f)
				lineTo(33.76f, 26.5f)
				verticalLineToRelative(25.8f)
				curveToRelative(0f, 1.93f, -1.57f, 3.5f, -3.5f, 3.5f)
				curveToRelative(-1.94f, 0f, -3.51f, -1.57f, -3.51f, -3.5f)
				close()
			}
			path(fill = SolidColor(Color.White)) {
				moveTo(68.79f, 12f)
				curveToRelative(-5.8f, 0f, -10.5f, 4.7f, -10.5f, 10.51f)
				curveToRelative(0f, 5.8f, 4.7f, 10.5f, 10.5f, 10.5f)
				reflectiveCurveToRelative(10.51f, -4.7f, 10.51f, -10.5f)
				reflectiveCurveTo(74.6f, 12f, 68.79f, 12f)
				close()
			}
		}.build()

		return _Navic!!
	}

@Suppress("ObjectPropertyName")
private var _Navic: ImageVector? = null
