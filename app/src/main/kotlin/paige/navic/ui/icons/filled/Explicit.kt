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

val Icons.Filled.Explicit: ImageVector
	get() {
		if (_Explicit != null) {
			return _Explicit!!
		}
		_Explicit = ImageVector.Builder(
			name = "Filled.Explicit",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(436.52f, 595.48f)
				verticalLineToRelative(-74.96f)
				lineTo(564f, 520.52f)
				quadToRelative(17f, 0f, 28.76f, -11.7f)
				quadToRelative(11.76f, -11.7f, 11.76f, -28.61f)
				reflectiveQuadToRelative(-11.76f, -28.82f)
				quadTo(581f, 439.48f, 564f, 439.48f)
				lineTo(436.52f, 439.48f)
				verticalLineToRelative(-74.96f)
				lineTo(564f, 364.52f)
				quadToRelative(17f, 0f, 28.76f, -11.7f)
				quadToRelative(11.76f, -11.7f, 11.76f, -28.61f)
				reflectiveQuadToRelative(-11.76f, -28.82f)
				quadTo(581f, 283.48f, 564f, 283.48f)
				lineTo(396f, 283.48f)
				quadToRelative(-17f, 0f, -28.76f, 11.76f)
				quadTo(355.48f, 307f, 355.48f, 324f)
				verticalLineToRelative(312f)
				quadToRelative(0f, 17f, 11.76f, 28.76f)
				quadTo(379f, 676.52f, 396f, 676.52f)
				horizontalLineToRelative(168f)
				quadToRelative(17f, 0f, 28.76f, -11.7f)
				quadToRelative(11.76f, -11.7f, 11.76f, -28.61f)
				reflectiveQuadToRelative(-11.76f, -28.82f)
				quadTo(581f, 595.48f, 564f, 595.48f)
				lineTo(436.52f, 595.48f)
				close()
				moveTo(222.78f, 835.22f)
				quadToRelative(-41f, 0f, -69.5f, -28.5f)
				reflectiveQuadToRelative(-28.5f, -69.5f)
				verticalLineToRelative(-514.44f)
				quadToRelative(0f, -41f, 28.5f, -69.5f)
				reflectiveQuadToRelative(69.5f, -28.5f)
				horizontalLineToRelative(514.44f)
				quadToRelative(41f, 0f, 69.5f, 28.5f)
				reflectiveQuadToRelative(28.5f, 69.5f)
				verticalLineToRelative(514.44f)
				quadToRelative(0f, 41f, -28.5f, 69.5f)
				reflectiveQuadToRelative(-69.5f, 28.5f)
				lineTo(222.78f, 835.22f)
				close()
			}
		}.build()

		return _Explicit!!
	}

@Suppress("ObjectPropertyName")
private var _Explicit: ImageVector? = null
