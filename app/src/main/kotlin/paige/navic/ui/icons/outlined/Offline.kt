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

val Icons.Outlined.Offline: ImageVector
	get() {
		if (_Offline != null) {
			return _Offline!!
		}
		_Offline = ImageVector.Builder(
			name = "Outlined.Offline",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(780f, 797f)
				lineToRelative(-55f, 55f)
				quadToRelative(-12f, 11f, -28.5f, 11.5f)
				reflectiveQuadTo(668f, 852f)
				quadToRelative(-12f, -12f, -12f, -28.5f)
				reflectiveQuadToRelative(12f, -28.5f)
				lineToRelative(56f, -55f)
				lineToRelative(-56f, -55f)
				quadToRelative(-12f, -12f, -12f, -28.5f)
				reflectiveQuadToRelative(12f, -28.5f)
				quadToRelative(12f, -12f, 28.5f, -12f)
				reflectiveQuadToRelative(28.5f, 12f)
				lineToRelative(55f, 56f)
				lineToRelative(55f, -56f)
				quadToRelative(12f, -12f, 28.5f, -12f)
				reflectiveQuadToRelative(28.5f, 12f)
				quadToRelative(12f, 12f, 12f, 28.5f)
				reflectiveQuadTo(892f, 685f)
				lineToRelative(-55f, 55f)
				lineToRelative(55f, 55f)
				quadToRelative(11f, 12f, 11.5f, 28.5f)
				reflectiveQuadTo(892f, 852f)
				quadToRelative(-12f, 12f, -28.5f, 12f)
				reflectiveQuadTo(835f, 852f)
				lineToRelative(-55f, -55f)
				close()
				moveTo(480f, 880f)
				quadToRelative(-83f, 0f, -156f, -31.5f)
				reflectiveQuadTo(197f, 763f)
				quadToRelative(-54f, -54f, -85.5f, -127f)
				reflectiveQuadTo(80f, 480f)
				quadToRelative(0f, -83f, 31.5f, -156f)
				reflectiveQuadTo(197f, 197f)
				quadToRelative(54f, -54f, 127f, -85.5f)
				reflectiveQuadTo(480f, 80f)
				quadToRelative(84f, 0f, 157f, 32f)
				reflectiveQuadToRelative(127f, 86.5f)
				quadToRelative(54f, 54.5f, 85f, 127f)
				reflectiveQuadTo(880f, 479f)
				quadToRelative(0f, 17f, -12.5f, 29f)
				reflectiveQuadTo(838f, 520f)
				quadToRelative(-16f, 0f, -27f, -11.5f)
				reflectiveQuadTo(800f, 480f)
				quadToRelative(0f, -20f, -2.5f, -40f)
				reflectiveQuadToRelative(-7.5f, -40f)
				lineTo(654f, 400f)
				quadToRelative(3f, 20f, 4.5f, 40f)
				reflectiveQuadToRelative(1.5f, 40f)
				quadToRelative(0f, 17f, -12f, 28.5f)
				reflectiveQuadTo(619f, 520f)
				quadToRelative(-17f, 0f, -28f, -11.5f)
				reflectiveQuadTo(580f, 480f)
				quadToRelative(0f, -20f, -1.5f, -40f)
				reflectiveQuadToRelative(-4.5f, -40f)
				lineTo(386f, 400f)
				quadToRelative(-3f, 20f, -4.5f, 40f)
				reflectiveQuadToRelative(-1.5f, 40f)
				quadToRelative(0f, 20f, 1.5f, 40f)
				reflectiveQuadToRelative(4.5f, 40f)
				horizontalLineToRelative(134f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(560f, 600f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(520f, 640f)
				lineTo(404f, 640f)
				quadToRelative(12f, 43f, 31f, 82.5f)
				reflectiveQuadToRelative(45f, 75.5f)
				quadToRelative(18f, 0f, 35.5f, -2f)
				reflectiveQuadToRelative(35.5f, -4f)
				quadToRelative(17f, -4f, 30.5f, 5f)
				reflectiveQuadToRelative(17.5f, 26f)
				quadToRelative(4f, 17f, -5f, 30f)
				reflectiveQuadToRelative(-26f, 17f)
				quadToRelative(-23f, 5f, -44f, 7.5f)
				reflectiveQuadTo(480f, 880f)
				close()
				moveTo(170f, 560f)
				horizontalLineToRelative(136f)
				quadToRelative(-3f, -20f, -4.5f, -40f)
				reflectiveQuadToRelative(-1.5f, -40f)
				quadToRelative(0f, -20f, 1.5f, -40f)
				reflectiveQuadToRelative(4.5f, -40f)
				lineTo(170f, 400f)
				quadToRelative(-5f, 20f, -7.5f, 40f)
				reflectiveQuadToRelative(-2.5f, 40f)
				quadToRelative(0f, 20f, 2.5f, 40f)
				reflectiveQuadToRelative(7.5f, 40f)
				close()
				moveTo(204f, 320f)
				horizontalLineToRelative(118f)
				quadToRelative(9f, -37f, 22.5f, -72.5f)
				reflectiveQuadTo(376f, 178f)
				quadToRelative(-55f, 18f, -99f, 54.5f)
				reflectiveQuadTo(204f, 320f)
				close()
				moveTo(376f, 782f)
				quadToRelative(-18f, -34f, -31.5f, -69.5f)
				reflectiveQuadTo(322f, 640f)
				lineTo(204f, 640f)
				quadToRelative(29f, 51f, 73f, 87.5f)
				reflectiveQuadToRelative(99f, 54.5f)
				close()
				moveTo(404f, 320f)
				horizontalLineToRelative(152f)
				quadToRelative(-12f, -43f, -31f, -82.5f)
				reflectiveQuadTo(480f, 162f)
				quadToRelative(-26f, 36f, -45f, 75.5f)
				reflectiveQuadTo(404f, 320f)
				close()
				moveTo(638f, 320f)
				horizontalLineToRelative(118f)
				quadToRelative(-29f, -51f, -73f, -87.5f)
				reflectiveQuadTo(584f, 178f)
				quadToRelative(18f, 34f, 31.5f, 69.5f)
				reflectiveQuadTo(638f, 320f)
				close()
			}
		}.build()

		return _Offline!!
	}

@Suppress("ObjectPropertyName")
private var _Offline: ImageVector? = null
