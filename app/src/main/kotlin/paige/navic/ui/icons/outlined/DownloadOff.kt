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

val Icons.Outlined.DownloadOff: ImageVector
	get() {
		if (_DownloadOff != null) {
			return _DownloadOff!!
		}
		_DownloadOff = ImageVector.Builder(
			name = "Outlined.DownloadOff",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(763f, 876f)
				lineToRelative(-77f, -76f)
				lineTo(240f, 800f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(160f, 720f)
				verticalLineToRelative(-80f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(200f, 600f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(240f, 640f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(366f)
				lineTo(503f, 617f)
				quadToRelative(-6f, 3f, -11.5f, 4.5f)
				reflectiveQuadTo(480f, 623f)
				quadToRelative(-10f, 0f, -16.5f, -3f)
				reflectiveQuadToRelative(-11.5f, -8f)
				lineTo(308f, 468f)
				quadToRelative(-11f, -11f, -11.5f, -25.5f)
				reflectiveQuadTo(304f, 418f)
				lineTo(83f, 197f)
				quadToRelative(-11f, -11f, -11f, -28f)
				reflectiveQuadToRelative(12f, -29f)
				quadToRelative(11f, -11f, 28f, -11f)
				reflectiveQuadToRelative(29f, 11f)
				lineToRelative(679f, 680f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				quadToRelative(-12f, 12f, -29f, 12f)
				reflectiveQuadToRelative(-28f, -12f)
				close()
				moveTo(652f, 412f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				lineToRelative(-7f, 7f)
				quadToRelative(-12f, 12f, -28f, 11.5f)
				reflectiveQuadTo(589f, 474f)
				quadToRelative(-12f, -12f, -12f, -28.5f)
				reflectiveQuadToRelative(12f, -28.5f)
				lineToRelative(6f, -6f)
				quadToRelative(11f, -11f, 28.5f, -10.5f)
				reflectiveQuadTo(652f, 412f)
				close()
				moveTo(508.5f, 171.5f)
				quadTo(520f, 183f, 520f, 200f)
				verticalLineToRelative(110f)
				quadToRelative(0f, 20f, -12.5f, 29.5f)
				reflectiveQuadTo(480f, 349f)
				quadToRelative(-15f, 0f, -27.5f, -10f)
				reflectiveQuadTo(440f, 309f)
				verticalLineToRelative(-109f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(480f, 160f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				close()
			}
		}.build()

		return _DownloadOff!!
	}

@Suppress("ObjectPropertyName")
private var _DownloadOff: ImageVector? = null
