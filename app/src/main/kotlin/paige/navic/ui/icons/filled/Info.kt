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

val Icons.Filled.Info: ImageVector
	get() {
		if (_Info != null) {
			return _Info!!
		}
		_Info = ImageVector.Builder(
			name = "Filled.Info",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(511.01f, 671.06f)
				quadToRelative(12.58f, -12.54f, 12.58f, -31.06f)
				verticalLineToRelative(-156.41f)
				quadToRelative(0f, -18.53f, -12.58f, -31.06f)
				quadTo(498.43f, 440f, 480f, 440f)
				reflectiveQuadToRelative(-31.01f, 12.53f)
				quadToRelative(-12.58f, 12.53f, -12.58f, 31.06f)
				lineTo(436.41f, 640f)
				quadToRelative(0f, 18.52f, 12.58f, 31.06f)
				quadToRelative(12.58f, 12.53f, 31.01f, 12.53f)
				reflectiveQuadToRelative(31.01f, -12.53f)
				close()
				moveTo(511.85f, 351.91f)
				quadToRelative(12.93f, -12.88f, 12.93f, -31.91f)
				reflectiveQuadToRelative(-12.87f, -31.91f)
				quadToRelative(-12.88f, -12.87f, -31.91f, -12.87f)
				reflectiveQuadToRelative(-31.91f, 12.87f)
				quadToRelative(-12.87f, 12.88f, -12.87f, 31.91f)
				reflectiveQuadToRelative(12.93f, 31.91f)
				quadToRelative(12.94f, 12.87f, 31.85f, 12.87f)
				quadToRelative(18.91f, 0f, 31.85f, -12.87f)
				close()
				moveTo(480f, 888.13f)
				quadToRelative(-84.91f, 0f, -159.34f, -32.12f)
				quadToRelative(-74.44f, -32.12f, -129.5f, -87.17f)
				quadToRelative(-55.05f, -55.06f, -87.17f, -129.5f)
				quadTo(71.87f, 564.91f, 71.87f, 480f)
				reflectiveQuadToRelative(32.12f, -159.34f)
				quadToRelative(32.12f, -74.44f, 87.17f, -129.5f)
				quadToRelative(55.06f, -55.05f, 129.5f, -87.17f)
				quadToRelative(74.43f, -32.12f, 159.34f, -32.12f)
				reflectiveQuadToRelative(159.34f, 32.12f)
				quadToRelative(74.44f, 32.12f, 129.5f, 87.17f)
				quadToRelative(55.05f, 55.06f, 87.17f, 129.5f)
				quadToRelative(32.12f, 74.43f, 32.12f, 159.34f)
				reflectiveQuadToRelative(-32.12f, 159.34f)
				quadToRelative(-32.12f, 74.44f, -87.17f, 129.5f)
				quadToRelative(-55.06f, 55.05f, -129.5f, 87.17f)
				quadTo(564.91f, 888.13f, 480f, 888.13f)
				close()
			}
		}.build()

		return _Info!!
	}

@Suppress("ObjectPropertyName")
private var _Info: ImageVector? = null
