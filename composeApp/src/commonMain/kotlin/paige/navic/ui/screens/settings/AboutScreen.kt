package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_app_version
import navic.composeapp.generated.resources.option_check_for_updates
import navic.composeapp.generated.resources.subtitle_check_for_updates
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_codeberg
import navic.composeapp.generated.resources.title_discord_server
import navic.composeapp.generated.resources.title_github
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalPlatformContext
import paige.navic.di.PlatformType
import paige.navic.domain.manager.PreferenceManager
import paige.navic.generated.BuildInfo
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.dialogs.LinkConfirmationDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem
import paige.navic.ui.screens.settings.components.SettingsToggleItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsAboutScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current
	val platformContext = LocalPlatformContext.current
	val hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	var linkToOpen by rememberSaveable { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_about)) },
				hideBack = hideBack
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
					append(platformContext.name + "\n")
					append(
						stringResource(
							Res.string.info_app_version,
							platformContext.appVersion
						)
					)
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
					content = { Text(stringResource(Res.string.title_github)) }
				)
				SettingsNavItem(
					onClick = { linkToOpen = "https://codeberg.org/paige/Navic" },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4),
					content = { Text(stringResource(Res.string.title_codeberg)) }
				)
				SettingsNavItem(
					onClick = { linkToOpen = "https://discord.gg/TBcnNX66PH" },
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3),
					content = { Text(stringResource(Res.string.title_discord_server)) }
				)
			}

			if (platformContext.platformType == PlatformType.Android && !BuildInfo.FDROID) {
				SettingsGroup {
					SettingsToggleItem(
						content = { Text(stringResource(Res.string.option_check_for_updates)) },
						supportingContent = { Text(stringResource(Res.string.subtitle_check_for_updates)) },
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
