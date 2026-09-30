/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.BottomBarCollapseMode
import paige.navic.domain.model.settings.BottomBarVisibilityMode
import paige.navic.domain.model.settings.MiniPlayerProgressStyle
import paige.navic.domain.model.settings.MiniPlayerStyle
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.NestedTopBarDefaults
import paige.navic.ui.screen.settings.component.SettingsChoiceItem
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsNavItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem
import paige.navic.ui.screen.settings.dialog.NavtabsDialog

@Composable
fun BottomBarScreen() {
	val sizeClass = LocalSizeClass.current
	val hideBack = sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	var tabsDialogOpen by rememberSaveable { mutableStateOf(false) }
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_bottom_app_bar)) },
				navigationAction = {
					if (!hideBack) {
						NestedTopBarDefaults.NavigationAction()
					}
				}
			)
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp),
				verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
			) {
				SettingsGroup {
					val count = if (!hideBack) 2 else 1
					SettingsChoiceItem(
						choices = BottomBarCollapseMode.entries.toImmutableList(),
						selectedChoice = preferenceManager.bottomBarCollapseMode,
						onChoiceSelected = { preferenceManager.bottomBarCollapseMode = it },
						content = { Text(stringResource(R.string.option_bottom_bar_collapse_mode)) },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = count)
					)

					if (!hideBack) {
						SettingsChoiceItem(
							choices = BottomBarVisibilityMode.entries.toImmutableList(),
							selectedChoice = preferenceManager.bottomBarVisibilityMode,
							onChoiceSelected = { preferenceManager.bottomBarVisibilityMode = it },
							content = { Text(stringResource(R.string.option_bottom_bar_visibility_mode)) },
							label = { stringResource(it.displayName) },
							shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = count)
						)
					}
				}

				SettingsGroup(title = { Text(stringResource(R.string.title_navigation_bar)) }) {
					SettingsNavItem(
						onClick = { tabsDialogOpen = true },
						content = { Text(stringResource(R.string.option_navigation_bar_tabs)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1)
					)
				}

				SettingsGroup(title = { Text(stringResource(R.string.title_mini_player)) }) {
					SettingsChoiceItem(
						choices = MiniPlayerStyle.entries.toImmutableList(),
						selectedChoice = preferenceManager.miniPlayerStyle,
						onChoiceSelected = { preferenceManager.miniPlayerStyle = it },
						content = { Text(stringResource(R.string.option_mini_player_style)) },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 4)
					)
					SettingsChoiceItem(
						choices = MiniPlayerProgressStyle.entries.toImmutableList(),
						selectedChoice = preferenceManager.miniPlayerProgressStyle,
						onChoiceSelected = { preferenceManager.miniPlayerProgressStyle = it },
						content = { Text(stringResource(R.string.option_mini_player_progress_style)) },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4)
					)
					SettingsToggleItem(
						content = { Text(stringResource(R.string.option_swipe_to_skip)) },
						checked = preferenceManager.swipeToSkip,
						onCheckedChange = { preferenceManager.swipeToSkip = it },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 4)
					)
					SettingsToggleItem(
						content = { Text(stringResource(R.string.option_hide_bottom_bar_if_idle)) },
						checked = preferenceManager.hideIfIdle,
						onCheckedChange = { preferenceManager.hideIfIdle = it },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 3, count = 4)
					)
				}
			}
		}
	}

	NavtabsDialog(
		presented = tabsDialogOpen,
		onDismissRequest = { tabsDialogOpen = false }
	)
}
