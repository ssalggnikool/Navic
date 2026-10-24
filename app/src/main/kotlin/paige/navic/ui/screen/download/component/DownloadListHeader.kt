/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.download.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.KeyboardArrowDown
import paige.navic.ui.theme.defaultFont

@Composable
fun DownloadListHeader(
	text: @Composable () -> Unit,
	expanded: Boolean,
	onExpandedChanged: (Boolean) -> Unit
) {
	val arrowRotation by animateFloatAsState(
		if (expanded) 0f else -90f
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onExpandedChanged(!expanded) }
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(modifier = Modifier.weight(1f)) {
				CompositionLocalProvider(
					LocalTextStyle provides MaterialTheme.typography.titleSmallEmphasized.copy(
						fontFamily = defaultFont(grade = 100, round = 100f)
					),
					LocalContentColor provides MaterialTheme.colorScheme.primary
				) {
					text()
				}
			}
			Icon(
				imageVector = Icons.Outlined.KeyboardArrowDown,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.rotate(arrowRotation).size(18.dp)
			)
		}
	}
}
