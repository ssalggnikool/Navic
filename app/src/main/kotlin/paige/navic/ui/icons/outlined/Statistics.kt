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

val Icons.Outlined.Statistics: ImageVector
	get() {
		if (_Statistics != null) {
			return _Statistics!!
		}
		_Statistics = ImageVector.Builder(
			name = "Outlined.Statistics",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(291.5f, 491.5f)
				quadTo(280f, 503f, 280f, 520f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(320f, 680f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(360f, 640f)
				verticalLineToRelative(-120f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(320f, 480f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				close()
				moveTo(611.5f, 291.5f)
				quadTo(600f, 303f, 600f, 320f)
				verticalLineToRelative(320f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(640f, 680f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(680f, 640f)
				verticalLineToRelative(-320f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(640f, 280f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				close()
				moveTo(451.5f, 571.5f)
				quadTo(440f, 583f, 440f, 600f)
				verticalLineToRelative(40f)
				quadToRelative(0f, 17f, 11.5f, 28.5f)
				reflectiveQuadTo(480f, 680f)
				quadToRelative(17f, 0f, 28.5f, -11.5f)
				reflectiveQuadTo(520f, 640f)
				verticalLineToRelative(-40f)
				quadToRelative(0f, -17f, -11.5f, -28.5f)
				reflectiveQuadTo(480f, 560f)
				quadToRelative(-17f, 0f, -28.5f, 11.5f)
				close()
				moveTo(200f, 840f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(120f, 760f)
				verticalLineToRelative(-560f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(200f, 120f)
				horizontalLineToRelative(560f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(840f, 200f)
				verticalLineToRelative(560f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(760f, 840f)
				lineTo(200f, 840f)
				close()
				moveTo(200f, 760f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-560f)
				lineTo(200f, 200f)
				verticalLineToRelative(560f)
				close()
				moveTo(200f, 200f)
				verticalLineToRelative(560f)
				verticalLineToRelative(-560f)
				close()
				moveTo(508.5f, 468.5f)
				quadTo(520f, 457f, 520f, 440f)
				reflectiveQuadToRelative(-11.5f, -28.5f)
				quadTo(497f, 400f, 480f, 400f)
				reflectiveQuadToRelative(-28.5f, 11.5f)
				quadTo(440f, 423f, 440f, 440f)
				reflectiveQuadToRelative(11.5f, 28.5f)
				quadTo(463f, 480f, 480f, 480f)
				reflectiveQuadToRelative(28.5f, -11.5f)
				close()
			}
		}.build()

		return _Statistics!!
	}

@Suppress("ObjectPropertyName")
private var _Statistics: ImageVector? = null
