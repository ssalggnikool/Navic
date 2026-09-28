package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.dropUnlessResumed
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.NestedTopBarDefaults
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem

@Composable
fun SettingsDeveloperScreen() {
	val platformContext = LocalPlatformContext.current
	val hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	val backStack = LocalNavStack.current
	var exceptionConfirmationShown by rememberSaveable { mutableStateOf(false) }
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_developer)) },
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
					SettingsNavItem(
						onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Settings.Developer) {
									backStack.add(Screen.Settings.ConnectionOptions)
								}
							}
						},
						content = { Text(stringResource(R.string.option_connection_options)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 3)
					)

					SettingsNavItem(
						onClick = {
							preferenceManager.shushQueueDuplicateDialog = false
						},
						content = { Text(stringResource(R.string.action_reset_dont_show_agains)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 3)
					)

					SettingsNavItem(
						onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Settings.Developer) {
									backStack.add(Screen.Settings.Logs)
								}
							}
						},
						content = { Text(stringResource(R.string.title_logs)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3)
					)
				}

				SettingsGroup {
					SegmentedListItem(
						onClick = { exceptionConfirmationShown = true },
						content = { Text(stringResource(R.string.action_test_exception_handler)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1),
						colors = SegmentedListItemDefaults.segmentedErrorColors()
					)
				}
			}
		}
	}

	if (exceptionConfirmationShown) {
		FormDialog(
			onDismissRequest = { exceptionConfirmationShown = false },
			title = { Text(stringResource(R.string.title_confirm)) },
			content = { Text(stringResource(R.string.info_exception_handler)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						exceptionConfirmationShown = false
						throw Error("Testing exception handler")
					},
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.errorColors()
				) {
					Text(stringResource(R.string.action_ok))
				}
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = { exceptionConfirmationShown = false },
					shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
				) {
					Text(stringResource(R.string.action_cancel))
				}
			},
		)
	}
}
