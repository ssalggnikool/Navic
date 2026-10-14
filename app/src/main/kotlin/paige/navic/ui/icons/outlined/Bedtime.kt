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

val Icons.Outlined.Bedtime: ImageVector
	get() {
		if (_Bedtime != null) {
			return _Bedtime!!
		}
		_Bedtime = ImageVector.Builder(
			name = "Outlined.Bedtime",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(484f, 880f)
				quadToRelative(-84f, 0f, -157.5f, -32f)
				reflectiveQuadToRelative(-128f, -86.5f)
				quadTo(144f, 707f, 112f, 633.5f)
				reflectiveQuadTo(80f, 476f)
				quadToRelative(0f, -146f, 93f, -257.5f)
				reflectiveQuadTo(410f, 80f)
				quadToRelative(-18f, 99f, 11f, 193.5f)
				reflectiveQuadTo(521f, 439f)
				quadToRelative(71f, 71f, 165.5f, 100f)
				reflectiveQuadTo(880f, 550f)
				quadToRelative(-26f, 144f, -138f, 237f)
				reflectiveQuadTo(484f, 880f)
				close()
				moveTo(484f, 800f)
				quadToRelative(88f, 0f, 163f, -44f)
				reflectiveQuadToRelative(118f, -121f)
				quadToRelative(-86f, -8f, -163f, -43.5f)
				reflectiveQuadTo(464f, 495f)
				quadToRelative(-61f, -61f, -97f, -138f)
				reflectiveQuadToRelative(-43f, -163f)
				quadToRelative(-77f, 43f, -120.5f, 118.5f)
				reflectiveQuadTo(160f, 476f)
				quadToRelative(0f, 135f, 94.5f, 229.5f)
				reflectiveQuadTo(484f, 800f)
				close()
				moveTo(464f, 495f)
				close()
			}
		}.build()

		return _Bedtime!!
	}

@Suppress("ObjectPropertyName")
private var _Bedtime: ImageVector? = null
