/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalSizeClass
import paige.navic.domain.manager.PreferenceManager
import paige.navic.generated.BuildInfo
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.dialog.LinkConfirmationDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.NestedTopBarDefaults
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsNavItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem

@Composable
fun SettingsAboutScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	val context = LocalContext.current
	val sizeClass = LocalSizeClass.current
	val hideBack = sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_about)) },
				navigationAction = {
					if (!hideBack) {
						NestedTopBarDefaults.NavigationAction()
					}
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
		) {
			SettingsGroup {
				val text = buildString {
					append(
						stringResource(
							R.string.info_app_version,
							context.packageManager
								.getPackageInfo(context.packageName, 0)
								.versionName.toString()
						) + "\n"
					)
					append("Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
				}
				SegmentedListItem(
					onClick = { clipboard.setText(AnnotatedString(text)) },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1),
					content = { Text(text) }
				)
			}

			SettingsGroup {
				SettingsNavItem(
					onClick = { linkToOpen = "https://github.com/ssalggnikool/Navic" },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 4),
					content = { Text(stringResource(R.string.title_github)) }
				)
				SettingsNavItem(
					onClick = { linkToOpen = "https://codeberg.org/paige/Navic" },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4),
					content = { Text(stringResource(R.string.title_codeberg)) }
				)
				SettingsNavItem(
					onClick = { linkToOpen = "https://discord.gg/TBcnNX66PH" },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3),
					content = { Text(stringResource(R.string.title_discord_server)) }
				)
			}

			if (!BuildInfo.FDROID) {
				SettingsGroup {
					SettingsToggleItem(
						content = { Text(stringResource(R.string.option_check_for_updates)) },
						supportingContent = { Text(stringResource(R.string.subtitle_check_for_updates)) },
						checked = preferenceManager.checkForUpdates,
						onCheckedChange = { preferenceManager.checkForUpdates = it },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1)
					)
				}
			}
		}
	}

	if (linkToOpen != null) {
		LinkConfirmationDialog(
			linkToOpen = linkToOpen!!,
			onDismissRequest = { linkToOpen = null }
		)
	}
}
