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

val Icons.Filled.RepeatOn: ImageVector
	get() {
		if (_RepeatOn != null) {
			return _RepeatOn!!
		}
		_RepeatOn = ImageVector.Builder(
			name = "Filled.RepeatOn",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(120f, 920f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(40f, 840f)
				verticalLineToRelative(-720f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(120f, 40f)
				horizontalLineToRelative(720f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(920f, 120f)
				verticalLineToRelative(720f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(840f, 920f)
				lineTo(120f, 920f)
				close()
				moveTo(274f, 760f)
				horizontalLineToRelative(406f)
				quadToRelative(33f, 0f, 56.5f, -23.5f)
				reflectiveQuadTo(760f, 680f)
				verticalLineToRelative(-120f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(720f, 520f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				reflectiveQuadTo(680f, 560f)
				verticalLineToRelative(120f)
				lineTo(274f, 680f)
				lineToRelative(34f, -34f)
				quadToRelative(12f, -12f, 11.5f, -28f)
				reflectiveQuadTo(308f, 590f)
				quadToRelative(-12f, -12f, -28.5f, -12.5f)
				reflectiveQuadTo(251f, 589f)
				lineTo(148f, 692f)
				quadToRelative(-6f, 6f, -8.5f, 13f)
				reflectiveQuadToRelative(-2.5f, 15f)
				quadToRelative(0f, 8f, 2.5f, 15f)
				reflectiveQuadToRelative(8.5f, 13f)
				lineToRelative(103f, 103f)
				quadToRelative(12f, 12f, 28.5f, 11.5f)
				reflectiveQuadTo(308f, 850f)
				quadToRelative(11f, -12f, 11.5f, -28f)
				reflectiveQuadTo(308f, 794f)
				lineToRelative(-34f, -34f)
				close()
				moveTo(686f, 280f)
				lineTo(652f, 314f)
				quadToRelative(-12f, 12f, -11.5f, 28f)
				reflectiveQuadToRelative(11.5f, 28f)
				quadToRelative(12f, 12f, 28.5f, 12.5f)
				reflectiveQuadTo(709f, 371f)
				lineToRelative(103f, -103f)
				quadToRelative(6f, -6f, 8.5f, -13f)
				reflectiveQuadToRelative(2.5f, -15f)
				quadToRelative(0f, -8f, -2.5f, -15f)
				reflectiveQuadToRelative(-8.5f, -13f)
				lineTo(709f, 109f)
				quadToRelative(-12f, -12f, -28.5f, -11.5f)
				reflectiveQuadTo(652f, 110f)
				quadToRelative(-11f, 12f, -11.5f, 28f)
				reflectiveQuadToRelative(11.5f, 28f)
				lineToRelative(34f, 34f)
				lineTo(280f, 200f)
				quadToRelative(-33f, 0f, -56.5f, 23.5f)
				reflectiveQuadTo(200f, 280f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(240f, 440f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(280f, 400f)
				verticalLineToRelative(-120f)
				horizontalLineToRelative(406f)
				close()
			}
		}.build()

		return _RepeatOn!!
	}

@Suppress("ObjectPropertyName")
private var _RepeatOn: ImageVector? = null
