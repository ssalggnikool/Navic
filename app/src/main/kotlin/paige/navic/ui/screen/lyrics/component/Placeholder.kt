/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.lyrics.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import paige.navic.R
import paige.navic.ui.component.common.ContentUnavailable

@Composable
fun LyricsScreenPlaceholder(
	onRefresh: () -> Unit
) {

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
	) {
		ContentUnavailable(
			modifier = Modifier,
			label = stringResource(R.string.info_no_lyrics)
		)

		TextButton(onClick = dropUnlessResumed {
			onRefresh()
		}) {
			Text(stringResource(R.string.action_refresh))
		}
	}
}
