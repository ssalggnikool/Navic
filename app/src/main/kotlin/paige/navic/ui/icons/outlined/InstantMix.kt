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

val Icons.Outlined.InstantMix: ImageVector
	get() {
		if (_InstantMix != null) {
			return _InstantMix!!
		}
		_InstantMix = ImageVector.Builder(
			name = "Outlined.InstantMix",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(7f, 17f)
				verticalLineToRelative(-10f)
				quadToRelative(0f, -0.425f, 0.2875f, -0.7125f)
				reflectiveQuadTo(8f, 6f)
				quadToRelative(0.425f, 0f, 0.7125f, 0.2875f)
				reflectiveQuadTo(9f, 7f)
				verticalLineToRelative(10f)
				quadToRelative(0f, 0.425f, -0.2875f, 0.7125f)
				reflectiveQuadTo(8f, 18f)
				quadToRelative(-0.425f, 0f, -0.7125f, -0.2875f)
				reflectiveQuadTo(7f, 17f)
				close()
				moveTo(11f, 21f)
				verticalLineToRelative(-18f)
				quadToRelative(0f, -0.425f, 0.2875f, -0.7125f)
				reflectiveQuadTo(12f, 2f)
				quadToRelative(0.425f, 0f, 0.7125f, 0.2875f)
				reflectiveQuadTo(13f, 3f)
				verticalLineToRelative(18f)
				quadToRelative(0f, 0.425f, -0.2875f, 0.7125f)
				reflectiveQuadTo(12f, 22f)
				quadToRelative(-0.425f, 0f, -0.7125f, -0.2875f)
				reflectiveQuadTo(11f, 21f)
				close()
				moveTo(3f, 13f)
				verticalLineToRelative(-2f)
				quadToRelative(0f, -0.425f, 0.2875f, -0.7125f)
				reflectiveQuadTo(4f, 10f)
				quadToRelative(0.425f, 0f, 0.7125f, 0.2875f)
				reflectiveQuadTo(5f, 11f)
				verticalLineToRelative(2f)
				quadToRelative(0f, 0.425f, -0.2875f, 0.7125f)
				reflectiveQuadTo(4f, 14f)
				quadToRelative(-0.425f, 0f, -0.7125f, -0.2875f)
				reflectiveQuadTo(3f, 13f)
				close()
				moveTo(15f, 17f)
				verticalLineToRelative(-10f)
				quadToRelative(0f, -0.425f, 0.2875f, -0.7125f)
				reflectiveQuadTo(16f, 6f)
				quadToRelative(0.425f, 0f, 0.7125f, 0.2875f)
				reflectiveQuadTo(17f, 7f)
				verticalLineToRelative(10f)
				quadToRelative(0f, 0.425f, -0.2875f, 0.7125f)
				reflectiveQuadTo(16f, 18f)
				quadToRelative(-0.425f, 0f, -0.7125f, -0.2875f)
				reflectiveQuadTo(15f, 17f)
				close()
				moveTo(19f, 13f)
				verticalLineToRelative(-2f)
				quadToRelative(0f, -0.425f, 0.2875f, -0.7125f)
				reflectiveQuadTo(20f, 10f)
				quadToRelative(0.425f, 0f, 0.7125f, 0.2875f)
				reflectiveQuadTo(21f, 11f)
				verticalLineToRelative(2f)
				quadToRelative(0f, 0.425f, -0.2875f, 0.7125f)
				reflectiveQuadTo(20f, 14f)
				quadToRelative(-0.425f, 0f, -0.7125f, -0.2875f)
				reflectiveQuadTo(19f, 13f)
				close()
			}
		}.build()

		return _InstantMix!!
	}

@Suppress("ObjectPropertyName")
private var _InstantMix: ImageVector? = null
