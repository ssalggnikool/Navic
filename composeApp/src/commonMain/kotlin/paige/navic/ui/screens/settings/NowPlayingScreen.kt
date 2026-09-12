package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_configure_lyric_providers
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.option_cover_art_action
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_lyrics_blur
import navic.composeapp.generated.resources.option_lyrics_bright_inactive
import navic.composeapp.generated.resources.option_lyrics_keep_alive
import navic.composeapp.generated.resources.option_now_playing_background_style
import navic.composeapp.generated.resources.option_now_playing_slider_style
import navic.composeapp.generated.resources.option_now_playing_song_info
import navic.composeapp.generated.resources.option_now_playing_toolbar_position
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.subtitle_configure_lyric_providers
import navic.composeapp.generated.resources.subtitle_now_playing_background_style
import navic.composeapp.generated.resources.title_layout
import navic.composeapp.generated.resources.title_now_playing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.CoverArtTapAction
import paige.navic.domain.models.settings.NowPlayingBackgroundStyle
import paige.navic.domain.models.settings.ToolbarPosition
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.NestedTopBarDefaults
import paige.navic.ui.screens.settings.components.SettingsChoiceItem
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem
import paige.navic.ui.screens.settings.components.SettingsToggleItem
import paige.navic.ui.screens.settings.dialogs.LyricsPrioritySheet
import paige.navic.ui.screens.settings.dialogs.NowPlayingSliderStyleDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsNowPlayingScreen() {
	val platformContext = LocalPlatformContext.current
	val hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	val preferenceManager = koinInject<PreferenceManager>()
	var lyricProvidersSheetOpen by rememberSaveable { mutableStateOf(false) }
	var sliderStyleDialogOpen by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.title_now_playing)) },
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
					SettingsToggleItem(
						checked = preferenceManager.swipeToSkip,
						onCheckedChange = { preferenceManager.swipeToSkip = it },
						content = { Text(stringResource(Res.string.option_swipe_to_skip)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 4)
					)
					SettingsChoiceItem(
						content = { Text(stringResource(Res.string.option_cover_art_action)) },
						choices = CoverArtTapAction.entries.toImmutableList(),
						selectedChoice = preferenceManager.nowPlayingCoverArtAction,
						onChoiceSelected = { preferenceManager.nowPlayingCoverArtAction = it },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 4)
					)
					SettingsChoiceItem(
						content = { Text(stringResource(Res.string.option_now_playing_background_style)) },
						description = stringResource(Res.string.subtitle_now_playing_background_style),
						choices = NowPlayingBackgroundStyle.entries.toImmutableList(),
						selectedChoice = preferenceManager.nowPlayingBackgroundStyle,
						onChoiceSelected = { preferenceManager.nowPlayingBackgroundStyle = it },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 4)
					)
					SegmentedListItem(
						onClick = { sliderStyleDialogOpen = true },
						content = { Text(stringResource(Res.string.option_now_playing_slider_style)) },
						supportingContent = { Text(stringResource(preferenceManager.nowPlayingSliderStyle.displayName)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 3, count = 4)
					)
				}

				SettingsGroup(title = { Text(stringResource(Res.string.action_lyrics)) }) {
					SettingsNavItem(
						onClick = { lyricProvidersSheetOpen = true },
						content = { Text(stringResource(Res.string.action_configure_lyric_providers)) },
						supportingContent = { Text(stringResource(Res.string.subtitle_configure_lyric_providers)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 6)
					)
					SettingsToggleItem(
						checked = preferenceManager.lyricsAutoscroll,
						onCheckedChange = { preferenceManager.lyricsAutoscroll = it },
						content = { Text(stringResource(Res.string.option_lyrics_autoscroll)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 6)
					)
					SettingsToggleItem(
						checked = preferenceManager.lyricsBeatByBeat,
						onCheckedChange = { preferenceManager.lyricsBeatByBeat = it },
						content = { Text(stringResource(Res.string.option_lyrics_beat_by_beat)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 6)
					)
					SettingsToggleItem(
						checked = preferenceManager.lyricsKeepAlive,
						onCheckedChange = { preferenceManager.lyricsKeepAlive = it },
						content = { Text(stringResource(Res.string.option_lyrics_keep_alive)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 3, count = 6)
					)
					SettingsToggleItem(
						checked = preferenceManager.lyricsBlur,
						onCheckedChange = { preferenceManager.lyricsBlur = it },
						content = { Text(stringResource(Res.string.option_lyrics_blur)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 4, count = 6)
					)
					SettingsToggleItem(
						checked = preferenceManager.lyricsBrightInactive,
						onCheckedChange = { preferenceManager.lyricsBrightInactive = it },
						content = { Text(stringResource(Res.string.option_lyrics_bright_inactive)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 5, count = 6)
					)
				}

				SettingsGroup(title = { Text(stringResource(Res.string.title_layout)) }) {
					SettingsToggleItem(
						checked = preferenceManager.nowPlayingSongInfo,
						onCheckedChange = { preferenceManager.nowPlayingSongInfo = it },
						content = { Text(stringResource(Res.string.option_now_playing_song_info)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 2)
					)
					SettingsChoiceItem(
						content = { Text(stringResource(Res.string.option_now_playing_toolbar_position)) },
						choices = ToolbarPosition.entries.toImmutableList(),
						selectedChoice = preferenceManager.nowPlayingToolbarPosition,
						onChoiceSelected = { preferenceManager.nowPlayingToolbarPosition = it },
						label = { stringResource(it.displayName) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 2)
					)
				}
			}
		}
	}

	LyricsPrioritySheet(
		presented = lyricProvidersSheetOpen,
		onDismissRequest = { lyricProvidersSheetOpen = false }
	)
	NowPlayingSliderStyleDialog(
		presented = sliderStyleDialogOpen,
		onDismissRequest = { sliderStyleDialogOpen = false }
	)
}
