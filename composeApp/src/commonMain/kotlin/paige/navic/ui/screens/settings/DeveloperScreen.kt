package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.action_reset_dont_show_agains
import navic.composeapp.generated.resources.action_test_exception_handler
import navic.composeapp.generated.resources.info_exception_handler
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.title_confirm
import navic.composeapp.generated.resources.title_developer
import navic.composeapp.generated.resources.title_logs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalPlatformContext
import paige.navic.di.PlatformType
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
				title = { Text(stringResource(Res.string.title_developer)) },
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
					val isAndroid = platformContext.platformType == PlatformType.Android
					val count = if (isAndroid) 3 else 2

					SettingsNavItem(
						onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Settings.Developer) {
									backStack.add(Screen.Settings.CustomHeaders)
								}
							}
						},
						content = { Text(stringResource(Res.string.option_custom_headers)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = count)
					)

					SettingsNavItem(
						onClick = {
							preferenceManager.shushQueueDuplicateDialog = false
						},
						content = { Text(stringResource(Res.string.action_reset_dont_show_agains)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = count)
					)

					if (isAndroid) {
						SettingsNavItem(
							onClick = dropUnlessResumed {
								backStack.lastOrNull()?.let {
									if (it is Screen.Settings.Developer) {
										backStack.add(Screen.Settings.Logs)
									}
								}
							},
							content = { Text(stringResource(Res.string.title_logs)) },
							shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = count)
						)
					}
				}

				SettingsGroup {
					SegmentedListItem(
						onClick = { exceptionConfirmationShown = true },
						content = { Text(stringResource(Res.string.action_test_exception_handler)) },
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
			title = { Text(stringResource(Res.string.title_confirm)) },
			content = { Text(stringResource(Res.string.info_exception_handler)) },
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
					Text(stringResource(Res.string.action_ok))
				}
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = { exceptionConfirmationShown = false },
					shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
				) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
		)
	}
}
