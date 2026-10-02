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

val Icons.Outlined.ListArrow: ImageVector
	get() {
		if (_ListArrow != null) {
			return _ListArrow!!
		}
		_ListArrow = ImageVector.Builder(
			name = "Outlined.ListArrow",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(225f, 780.5f)
				quadToRelative(-7f, -2.5f, -13f, -8.5f)
				lineTo(108f, 668f)
				quadToRelative(-11f, -11f, -11.5f, -27.5f)
				reflectiveQuadTo(108f, 612f)
				quadToRelative(11f, -11f, 27.5f, -11.5f)
				reflectiveQuadTo(164f, 611f)
				lineToRelative(36f, 35f)
				verticalLineToRelative(-406f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(240f, 200f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(280f, 240f)
				verticalLineToRelative(406f)
				lineToRelative(36f, -35f)
				quadToRelative(11f, -11f, 27.5f, -11f)
				reflectiveQuadToRelative(28.5f, 12f)
				quadToRelative(11f, 11f, 11f, 28f)
				reflectiveQuadToRelative(-11f, 28f)
				lineTo(268f, 772f)
				quadToRelative(-6f, 6f, -13f, 8.5f)
				reflectiveQuadToRelative(-15f, 2.5f)
				quadToRelative(-8f, 0f, -15f, -2.5f)
				close()
				moveTo(520f, 760f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(480f, 720f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(520f, 680f)
				horizontalLineToRelative(320f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(880f, 720f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(840f, 760f)
				lineTo(520f, 760f)
				close()
				moveTo(520f, 520f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(480f, 480f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(520f, 440f)
				horizontalLineToRelative(320f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(880f, 480f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(840f, 520f)
				lineTo(520f, 520f)
				close()
				moveTo(520f, 280f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(480f, 240f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(520f, 200f)
				horizontalLineToRelative(320f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(880f, 240f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(840f, 280f)
				lineTo(520f, 280f)
				close()
			}
		}.build()

		return _ListArrow!!
	}

@Suppress("ObjectPropertyName")
private var _ListArrow: ImageVector? = null
