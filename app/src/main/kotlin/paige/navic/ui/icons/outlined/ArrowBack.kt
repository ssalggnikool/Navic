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

val Icons.Outlined.ArrowBack: ImageVector
	get() {
		if (_ArrowBack != null) {
			return _ArrowBack!!
		}
		_ArrowBack = ImageVector.Builder(
			name = "Outlined.ArrowBack",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveToRelative(313f, 520f)
				lineToRelative(196f, 196f)
				quadToRelative(12f, 12f, 11.5f, 28f)
				reflectiveQuadTo(508f, 772f)
				quadToRelative(-12f, 11f, -28f, 11.5f)
				reflectiveQuadTo(452f, 772f)
				lineTo(188f, 508f)
				quadToRelative(-6f, -6f, -8.5f, -13f)
				reflectiveQuadToRelative(-2.5f, -15f)
				quadToRelative(0f, -8f, 2.5f, -15f)
				reflectiveQuadToRelative(8.5f, -13f)
				lineToRelative(264f, -264f)
				quadToRelative(11f, -11f, 27.5f, -11f)
				reflectiveQuadToRelative(28.5f, 11f)
				quadToRelative(12f, 12f, 12f, 28.5f)
				reflectiveQuadTo(508f, 245f)
				lineTo(313f, 440f)
				horizontalLineToRelative(447f)
				quadToRelative(17f, 0f, 28.5f, 11.5f)
				reflectiveQuadTo(800f, 480f)
				quadToRelative(0f, 17f, -11.5f, 28.5f)
				reflectiveQuadTo(760f, 520f)
				lineTo(313f, 520f)
				close()
			}
		}.build()

		return _ArrowBack!!
	}

@Suppress("ObjectPropertyName")
private var _ArrowBack: ImageVector? = null
