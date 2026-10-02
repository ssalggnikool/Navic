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

val Icons.Filled.SkipPrevious: ImageVector
	get() {
		if (_SkipPrevious != null) {
			return _SkipPrevious!!
		}
		_SkipPrevious = ImageVector.Builder(
			name = "Filled.SkipPrevious",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(220f, 680f)
				verticalLineToRelative(-400f)
				quadToRelative(0f, -17f, 11.5f, -28.5f)
				reflectiveQuadTo(260f, 240f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(300f, 280f)
				verticalLineToRelative(400f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(260f, 720f)
				quadToRelative(-17f, 0f, -28.5f, -11.5f)
				reflectiveQuadTo(220f, 680f)
				close()
				moveTo(678f, 679f)
				lineTo(430f, 513f)
				quadToRelative(-9f, -6f, -13.5f, -14.5f)
				reflectiveQuadTo(412f, 480f)
				quadToRelative(0f, -10f, 4.5f, -18.5f)
				reflectiveQuadTo(430f, 447f)
				lineToRelative(248f, -166f)
				quadToRelative(5f, -4f, 11f, -5f)
				reflectiveQuadToRelative(11f, -1f)
				quadToRelative(16f, 0f, 28f, 11f)
				reflectiveQuadToRelative(12f, 29f)
				verticalLineToRelative(330f)
				quadToRelative(0f, 18f, -12f, 29f)
				reflectiveQuadToRelative(-28f, 11f)
				quadToRelative(-5f, 0f, -11f, -1f)
				reflectiveQuadToRelative(-11f, -5f)
				close()
			}
		}.build()

		return _SkipPrevious!!
	}

@Suppress("ObjectPropertyName")
private var _SkipPrevious: ImageVector? = null
