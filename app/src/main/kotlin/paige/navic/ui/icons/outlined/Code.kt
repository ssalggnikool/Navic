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

val Icons.Outlined.Code: ImageVector
	get() {
		if (_Code != null) {
			return _Code!!
		}
		_Code = ImageVector.Builder(
			name = "Outlined.Code",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(160.89f, 480f)
				lineToRelative(111.18f, 111.17f)
				quadToRelative(12.67f, 13.68f, 13.05f, 32.33f)
				quadToRelative(0.38f, 18.65f, -13.29f, 32.33f)
				quadTo(258.15f, 669.5f, 240f, 669.5f)
				reflectiveQuadToRelative(-31.83f, -13.67f)
				lineToRelative(-144f, -143.76f)
				quadTo(50.5f, 498.39f, 50.5f, 480f)
				reflectiveQuadToRelative(13.67f, -32.07f)
				lineToRelative(144f, -143.76f)
				quadToRelative(13.68f, -13.67f, 31.95f, -13.67f)
				reflectiveQuadToRelative(31.95f, 13.67f)
				quadToRelative(13.67f, 13.68f, 13.55f, 32.33f)
				quadToRelative(-0.12f, 18.65f, -13.55f, 32.33f)
				lineTo(160.89f, 480f)
				close()
				moveTo(351.6f, 783.01f)
				quadToRelative(-8.58f, -16.53f, -2.86f, -34.68f)
				lineToRelative(176f, -564f)
				quadToRelative(5.72f, -18.16f, 22.25f, -26.73f)
				quadToRelative(16.53f, -8.58f, 34.68f, -2.86f)
				quadToRelative(18.16f, 5.72f, 26.73f, 22.25f)
				quadToRelative(8.58f, 16.53f, 2.86f, 34.68f)
				lineToRelative(-176f, 564f)
				quadToRelative(-5.72f, 18.16f, -22.25f, 26.73f)
				quadToRelative(-16.53f, 8.58f, -34.68f, 2.86f)
				quadToRelative(-18.16f, -5.72f, -26.73f, -22.25f)
				close()
				moveTo(799.11f, 480f)
				lineTo(687.93f, 368.83f)
				quadToRelative(-13.43f, -13.68f, -13.43f, -32.33f)
				reflectiveQuadToRelative(13.67f, -32.33f)
				quadTo(701.85f, 290.5f, 720f, 290.5f)
				reflectiveQuadToRelative(31.83f, 13.67f)
				lineToRelative(144f, 143.76f)
				quadTo(909.5f, 461.61f, 909.5f, 480f)
				reflectiveQuadToRelative(-13.67f, 32.07f)
				lineToRelative(-144f, 143.76f)
				quadToRelative(-13.68f, 13.67f, -31.95f, 13.67f)
				reflectiveQuadToRelative(-31.95f, -13.67f)
				quadToRelative(-13.67f, -13.68f, -13.55f, -32.33f)
				quadToRelative(0.12f, -18.65f, 13.55f, -32.33f)
				lineTo(799.11f, 480f)
				close()
			}
		}.build()

		return _Code!!
	}

@Suppress("ObjectPropertyName")
private var _Code: ImageVector? = null
