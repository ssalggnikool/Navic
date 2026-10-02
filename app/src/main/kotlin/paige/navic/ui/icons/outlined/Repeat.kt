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

val Icons.Outlined.Repeat: ImageVector
	get() {
		if (_Repeat != null) {
			return _Repeat!!
		}
		_Repeat = ImageVector.Builder(
			name = "Outlined.Repeat",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(274f, 760f)
				lineToRelative(34f, 34f)
				quadToRelative(12f, 12f, 11.5f, 28f)
				reflectiveQuadTo(308f, 850f)
				quadToRelative(-12f, 12f, -28.5f, 12.5f)
				reflectiveQuadTo(251f, 851f)
				lineTo(148f, 748f)
				quadToRelative(-6f, -6f, -8.5f, -13f)
				reflectiveQuadToRelative(-2.5f, -15f)
				quadToRelative(0f, -8f, 2.5f, -15f)
				reflectiveQuadToRelative(8.5f, -13f)
				lineToRelative(103f, -103f)
				quadToRelative(12f, -12f, 28.5f, -11.5f)
				reflectiveQuadTo(308f, 590f)
				quadToRelative(11f, 12f, 11.5f, 28f)
				reflectiveQuadTo(308f, 646f)
				lineToRelative(-34f, 34f)
				horizontalLineToRelative(406f)
				verticalLineToRelative(-120f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(720f, 520f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(760f, 560f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(680f, 760f)
				lineTo(274f, 760f)
				close()
				moveTo(686f, 280f)
				lineTo(280f, 280f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(240f, 440f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(200f, 400f)
				verticalLineToRelative(-120f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(280f, 200f)
				horizontalLineToRelative(406f)
				lineToRelative(-34f, -34f)
				quadToRelative(-12f, -12f, -11.5f, -28f)
				reflectiveQuadToRelative(11.5f, -28f)
				quadToRelative(12f, -12f, 28.5f, -12.5f)
				reflectiveQuadTo(709f, 109f)
				lineToRelative(103f, 103f)
				quadToRelative(6f, 6f, 8.5f, 13f)
				reflectiveQuadToRelative(2.5f, 15f)
				quadToRelative(0f, 8f, -2.5f, 15f)
				reflectiveQuadToRelative(-8.5f, 13f)
				lineTo(709f, 371f)
				quadToRelative(-12f, 12f, -28.5f, 11.5f)
				reflectiveQuadTo(652f, 370f)
				quadToRelative(-11f, -12f, -11.5f, -28f)
				reflectiveQuadToRelative(11.5f, -28f)
				lineToRelative(34f, -34f)
				close()
			}
		}.build()

		return _Repeat!!
	}

@Suppress("ObjectPropertyName")
private var _Repeat: ImageVector? = null
