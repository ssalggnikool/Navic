package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.ExplicitContentPlayback
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.NestedTopBarDefaults
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingsChoiceItem
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem
import paige.navic.ui.screens.settings.components.SettingsSliderItem
import paige.navic.ui.screens.settings.components.SettingsToggleItem
import kotlin.math.roundToInt

@Composable
fun SettingsPlaybackScreen() {
	val platformContext = LocalPlatformContext.current
	val hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	val backStack = LocalNavStack.current
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_playback)) },
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
						onClick = dropUnlessResumed { backStack.add(Screen.Settings.StreamingQuality) },
						content = { Text(stringResource(R.string.title_streaming_quality)) },
						supportingContent = { Text(stringResource(R.string.subtitle_streaming_quality)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 4)
					)
					SettingsChoiceItem(
						choices = ExplicitContentPlayback.entries.toImmutableList(),
						selectedChoice = preferenceManager.explicitContentPlayback,
						onChoiceSelected = { preferenceManager.explicitContentPlayback = it },
						content = { Text(stringResource(R.string.option_explicit_playback)) },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4)
					)
					SettingsNavItem(
						onClick = dropUnlessResumed { backStack.add(Screen.Settings.Effects) },
						content = { Text(stringResource(R.string.title_audio_effects)) },
						supportingContent = { Text(stringResource(R.string.subtitle_audio_effects)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 2,
							count = 4
						)
					)
					SettingsToggleItem(
						checked = preferenceManager.autoFillQueue,
						onCheckedChange = { preferenceManager.autoFillQueue = it },
						content = { Text(stringResource(R.string.option_auto_fill_queue)) },
						supportingContent = { Text(stringResource(R.string.subtitle_auto_fill_queue)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 3,
							count = 4
						)
					)
				}

				SettingsGroup(title = { Text(stringResource(R.string.title_behaviour)) }) {
					val enableScrobbling = preferenceManager.enableScrobbling
					val count = if (enableScrobbling) 3 else 1

					SettingsToggleItem(
						checked = enableScrobbling,
						onCheckedChange = { preferenceManager.enableScrobbling = it },
						content = { Text(stringResource(R.string.option_enable_scrobbling)) },
						supportingContent = { Text(stringResource(R.string.subtitle_enable_scrobbling)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = count)
					)

					AnimatedVisibility(visible = enableScrobbling) {
						SettingsSliderItem(
							value = preferenceManager.scrobblePercentage,
							valueRange = 0f..1f,
							onValueChange = { preferenceManager.scrobblePercentage = it },
							trailingContent = { Text("${(preferenceManager.scrobblePercentage * 100).roundToInt()}%") },
							content = { Text(stringResource(R.string.option_scrobble_percentage)) },
							shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = count)
						)
					}

					AnimatedVisibility(visible = enableScrobbling) {

						SettingsSliderItem(
							value = preferenceManager.minDurationToScrobble,
							valueRange = 0f..60f,
							onValueChange = { preferenceManager.minDurationToScrobble = it },
							trailingContent = { Text("${preferenceManager.minDurationToScrobble.toInt()}s") },
							content = { Text(stringResource(R.string.option_min_duration_to_scrobble)) },
							shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = count)
						)
					}
				}
			}
		}
	}
}
