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

val Icons.Outlined.Globe: ImageVector
	get() {
		if (_Globe != null) {
			return _Globe!!
		}
		_Globe = ImageVector.Builder(
			name = "Globe",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(12f, 22f)
				quadToRelative(-2.07f, 0f, -3.9f, -0.79f)
				quadToRelative(-1.82f, -0.79f, -3.17f, -2.14f)
				quadTo(3.58f, 17.73f, 2.79f, 15.9f)
				reflectiveQuadTo(2f, 12f)
				quadTo(2f, 9.92f, 2.79f, 8.1f)
				quadTo(3.58f, 6.27f, 4.93f, 4.93f)
				quadTo(6.28f, 3.57f, 8.1f, 2.79f)
				quadTo(9.93f, 2f, 12f, 2f)
				reflectiveQuadToRelative(3.9f, 0.79f)
				reflectiveQuadToRelative(3.17f, 2.14f)
				quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
				quadTo(22f, 9.92f, 22f, 12f)
				reflectiveQuadToRelative(-0.79f, 3.9f)
				reflectiveQuadToRelative(-2.14f, 3.17f)
				quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
				reflectiveQuadTo(12f, 22f)
				close()
				moveTo(12f, 20f)
				quadToRelative(3.35f, 0f, 5.68f, -2.32f)
				reflectiveQuadTo(20f, 12f)
				quadToRelative(0f, -0.18f, -0.01f, -0.36f)
				reflectiveQuadToRelative(-0.01f, -0.31f)
				quadToRelative(-0.13f, 0.72f, -0.68f, 1.2f)
				reflectiveQuadTo(18f, 13f)
				lineTo(16f, 13f)
				quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
				reflectiveQuadTo(14f, 11f)
				lineTo(14f, 10f)
				lineTo(10f, 10f)
				lineTo(10f, 8f)
				quadToRelative(0f, -0.82f, 0.59f, -1.41f)
				reflectiveQuadTo(12f, 6f)
				horizontalLineToRelative(1f)
				quadToRelative(0f, -0.57f, 0.31f, -1.01f)
				reflectiveQuadToRelative(0.76f, -0.71f)
				quadToRelative(-0.5f, -0.13f, -1.01f, -0.2f)
				reflectiveQuadTo(12f, 4f)
				quadTo(8.65f, 4f, 6.33f, 6.32f)
				reflectiveQuadTo(4f, 12f)
				lineTo(9f, 12f)
				quadToRelative(1.65f, 0f, 2.83f, 1.17f)
				reflectiveQuadTo(13f, 16f)
				verticalLineToRelative(1f)
				lineTo(10f, 17f)
				verticalLineToRelative(2.75f)
				quadToRelative(0.5f, 0.13f, 0.99f, 0.19f)
				reflectiveQuadTo(12f, 20f)
				close()
			}
		}.build()

		return _Globe!!
	}

@Suppress("ObjectPropertyName")
private var _Globe: ImageVector? = null
