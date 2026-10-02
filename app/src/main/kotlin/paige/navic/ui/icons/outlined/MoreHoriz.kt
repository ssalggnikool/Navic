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

val Icons.Outlined.MoreHoriz: ImageVector
	get() {
		if (_MoreHoriz != null) {
			return _MoreHoriz!!
		}
		_MoreHoriz = ImageVector.Builder(
			name = "Outlined.MoreHoriz",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color(0xFFE3E3E3))) {
				moveTo(240f, 560f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(160f, 480f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(240f, 400f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(320f, 480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(240f, 560f)
				close()
				moveTo(480f, 560f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(400f, 480f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(480f, 400f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(560f, 480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(480f, 560f)
				close()
				moveTo(720f, 560f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(640f, 480f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(720f, 400f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(800f, 480f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(720f, 560f)
				close()
			}
		}.build()

		return _MoreHoriz!!
	}

@Suppress("ObjectPropertyName")
private var _MoreHoriz: ImageVector? = null
