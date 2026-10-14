/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.icons.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons

val Icons.Brand.MusicBrainz: ImageVector
	get() {
		if (_MusicBrainz != null) {
			return _MusicBrainz!!
		}
		_MusicBrainz = ImageVector.Builder(
			name = "Brand.MusicBrainz",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 30f,
			viewportHeight = 30f
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(14.5f, 1f)
				lineToRelative(-12f, 7f)
				verticalLineToRelative(14f)
				lineToRelative(12f, 7f)
				close()
			}
			path(fill = SolidColor(Color.White)) {
				moveTo(15.5f, 1f)
				lineToRelative(12f, 7f)
				verticalLineToRelative(14f)
				lineToRelative(-12f, 7f)
				close()
			}
		}.build()

		return _MusicBrainz!!
	}

@Suppress("ObjectPropertyName")
private var _MusicBrainz: ImageVector? = null
