/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.lyrics.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.collections.immutable.ImmutableList
import paige.navic.R
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.ArrowBack
import paige.navic.ui.icons.outlined.Check
import paige.navic.ui.icons.outlined.KeyboardArrowDown
import paige.navic.ui.icons.outlined.Share
import paige.navic.ui.component.layout.TopBarButton
import paige.navic.ui.component.toolbar.SheetToolbar

@Composable
fun LyricsScreenToolbar(
	onDismissRequest: () -> Unit,
	onShare: () -> Unit,
	isSelecting: Boolean,
	toggleIsSelecting: () -> Unit,
	windowInsets: WindowInsets,
	selectedIndices: ImmutableList<Int>
) {
	SheetToolbar(
		windowInsets = windowInsets,
		navigationIcon = {
			TopBarButton(
				onClick = dropUnlessResumed {
					if (!isSelecting) {
						onDismissRequest()
					} else {
						toggleIsSelecting()
					}
				},
				content = {
					Icon(
						imageVector = if (!isSelecting)
							Icons.Outlined.KeyboardArrowDown
						else Icons.Outlined.ArrowBack,
						contentDescription = stringResource(R.string.action_navigate_back)
					)
				}
			)
			NavigationBackHandler(
				state = rememberNavigationEventState(NavigationEventInfo.None),
				isBackEnabled = isSelecting,
				onBackCompleted = toggleIsSelecting
			)
		},
		actions = {
			TopBarButton(
				enabled = !isSelecting || selectedIndices.isNotEmpty(),
				onClick = {
					if (isSelecting) {
						onShare()
					} else {
						toggleIsSelecting()
					}
				}
			) {
				Icon(
					imageVector = if (!isSelecting)
						Icons.Outlined.Share
					else Icons.Outlined.Check,
					contentDescription = stringResource(R.string.action_share_lyrics),
					modifier = Modifier.size(26.dp)
				)
			}
		},
		title = {
			if (isSelecting) {
				Column {
					Text(
						stringResource(R.string.title_select_lyrics),
						fontWeight = FontWeight.SemiBold
					)
					Text(
						pluralStringResource(
							R.plurals.count_lines,
							selectedIndices.count(),
							selectedIndices.count()
						)
					)
				}
			}
		}
	)
}
