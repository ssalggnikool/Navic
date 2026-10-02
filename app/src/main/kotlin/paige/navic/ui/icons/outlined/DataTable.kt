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

val Icons.Outlined.DataTable: ImageVector
	get() {
		if (_DataTable != null) {
			return _DataTable!!
		}
		_DataTable = ImageVector.Builder(
			name = "Outlined.DataTable",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 960f,
			viewportHeight = 960f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(212.31f, 820f)
				quadTo(182f, 820f, 161f, 799f)
				quadToRelative(-21f, -21f, -21f, -51.31f)
				verticalLineToRelative(-535.38f)
				quadTo(140f, 182f, 161f, 161f)
				quadToRelative(21f, -21f, 51.31f, -21f)
				horizontalLineToRelative(535.38f)
				quadTo(778f, 140f, 799f, 161f)
				quadToRelative(21f, 21f, 21f, 51.31f)
				verticalLineToRelative(535.38f)
				quadTo(820f, 778f, 799f, 799f)
				quadToRelative(-21f, 21f, -51.31f, 21f)
				lineTo(212.31f, 820f)
				close()
				moveTo(200f, 346.46f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-134.15f)
				quadToRelative(0f, -4.62f, -3.85f, -8.46f)
				quadToRelative(-3.84f, -3.85f, -8.46f, -3.85f)
				lineTo(212.31f, 200f)
				quadToRelative(-4.62f, 0f, -8.46f, 3.85f)
				quadToRelative(-3.85f, 3.84f, -3.85f, 8.46f)
				verticalLineToRelative(134.15f)
				close()
				moveTo(200f, 553.54f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-147.08f)
				lineTo(200f, 406.46f)
				verticalLineToRelative(147.08f)
				close()
				moveTo(212.31f, 760f)
				horizontalLineToRelative(535.38f)
				quadToRelative(4.62f, 0f, 8.46f, -3.85f)
				quadToRelative(3.85f, -3.84f, 3.85f, -8.46f)
				verticalLineToRelative(-134.15f)
				lineTo(200f, 613.54f)
				verticalLineToRelative(134.15f)
				quadToRelative(0f, 4.62f, 3.85f, 8.46f)
				quadToRelative(3.84f, 3.85f, 8.46f, 3.85f)
				close()
				moveTo(255.39f, 309.08f)
				verticalLineToRelative(-72.31f)
				horizontalLineToRelative(72.3f)
				verticalLineToRelative(72.31f)
				horizontalLineToRelative(-72.3f)
				close()
				moveTo(255.39f, 516.15f)
				verticalLineToRelative(-72.3f)
				horizontalLineToRelative(72.3f)
				verticalLineToRelative(72.3f)
				horizontalLineToRelative(-72.3f)
				close()
				moveTo(255.39f, 723.23f)
				verticalLineToRelative(-72.31f)
				horizontalLineToRelative(72.3f)
				verticalLineToRelative(72.31f)
				horizontalLineToRelative(-72.3f)
				close()
			}
		}.build()

		return _DataTable!!
	}

@Suppress("ObjectPropertyName")
private var _DataTable: ImageVector? = null
