/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.materialkolor.PaletteStyle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.repository.SongRepository
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.MiniPlayer
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.settings.component.SettingsChoiceItem
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsNavItem
import paige.navic.ui.util.label
import paige.navic.ui.util.rememberColorSchemeForCurrentSong
import paige.navic.ui.util.rememberDominantColorFromCoverArt

@Composable
fun SettingsPlayerDebuggingScreen() {
	val backStack = LocalNavStack.current
	val player = koinInject<MediaPlayerViewModel>()
	val songRepository = koinInject<SongRepository>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val currentSong = playerState.currentSong
	val coverArtId = currentSong?.coverArtId
	var paletteStyle by remember { mutableStateOf(PaletteStyle.Content) }
	val dominantColor = rememberDominantColorFromCoverArt(coverArtId)
	val colorScheme = rememberColorSchemeForCurrentSong(style = paletteStyle)
	val scope = rememberCoroutineScope()

	val onPlayRandomSong: () -> Unit = {
		scope.launch {
			songRepository.getRandomSongs(1).firstOrNull()?.let { song ->
				player.clearQueue()
				player.playNow(song)
			}
		}
	}

	Scaffold(
		topBar = { NestedTopBar({}) }
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
				SettingsGroup(title = { Text("Controls") }) {
					Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
						FilledTonalButton(onClick = onPlayRandomSong) {
							Text("Random song")
						}
						FilledTonalButton(onClick = player::clearQueue) {
							Text(stringResource(R.string.action_clear_queue))
						}
					}
					MiniPlayer(
						modifier = Modifier.padding(top = 16.dp),
						windowInsets = WindowInsets()
					)
				}
				SettingsGroup(title = { Text("Theme debugging") }) {
					SegmentedListItem(
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 3),
						content = { Text("Seed") },
						trailingContent = {
							Box(
								modifier = Modifier
									.background(
										dominantColor.color,
										MaterialTheme.shapes.medium
									)
									.size(48.dp)
							)
						},
						onClick = {}
					)
					SettingsChoiceItem(
						choices = PaletteStyle.entries.toImmutableList(),
						selectedChoice = paletteStyle,
						onChoiceSelected = { paletteStyle = it },
						content = { Text(stringResource(R.string.option_palette_style)) },
						label = { it.label() },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 3)
					)
					SettingsNavItem(
						onClick = dropUnlessResumed {
							if (backStack.lastOrNull() is Screen.Settings.PlayerDebugging) {
								backStack.add(Screen.Settings.Themes)
							}
						},
						content = { Text(stringResource(R.string.title_theme_mode)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3)
					)
				}
				Row(Modifier.clip(MaterialTheme.shapes.extraSmall)) {
					ColourSegmentThingy(
						color = colorScheme.primary,
						colorName = "primary",
						contentColor = colorScheme.onPrimary,
						contentColorName = "onPrimary"
					)
					ColourSegmentThingy(
						color = colorScheme.secondary,
						colorName = "secondary",
						contentColor = colorScheme.onSecondary,
						contentColorName = "onSecondary"
					)
					ColourSegmentThingy(
						color = colorScheme.tertiary,
						colorName = "tertiary",
						contentColor = colorScheme.onTertiary,
						contentColorName = "onTertiary"
					)
				}
				Row(Modifier.clip(MaterialTheme.shapes.extraSmall)) {
					ColourSegmentThingy(
						color = colorScheme.primaryContainer,
						colorName = "primaryContainer",
						contentColor = colorScheme.onPrimaryContainer,
						contentColorName = "onPrimaryContainer"
					)
					ColourSegmentThingy(
						color = colorScheme.secondaryContainer,
						colorName = "secondaryContainer",
						contentColor = colorScheme.onSecondaryContainer,
						contentColorName = "onSecondaryContainer"
					)
					ColourSegmentThingy(
						color = colorScheme.tertiaryContainer,
						colorName = "tertiaryContainer",
						contentColor = colorScheme.onTertiaryContainer,
						contentColorName = "onTertiaryContainer"
					)
				}
				SettingsGroup(title = { Text("Stuff (selectable)") }) {
					SelectionContainer {
						Column {
							Text("id: ${currentSong?.id}")
							Text("coverArtId: $coverArtId")
							Text("albumId: ${currentSong?.albumId}")
							Text("artistId: ${currentSong?.artistId}")
						}
					}
				}
			}
		}
	}
}

@Composable
private fun RowScope.ColourSegmentThingy(
	color: Color,
	colorName: String,
	contentColor: Color,
	contentColorName: String
) {
	Column(Modifier.weight(1f).height(48.dp)) {
		Box(Modifier.weight(1f).fillMaxWidth().background(color)) {
			Text(
				text = colorName,
				modifier = Modifier.align(Alignment.Center),
				color = contentColor,
				style = MaterialTheme.typography.bodySmall
			)
		}
		Box(Modifier.weight(1f).fillMaxWidth().background(contentColor)) {
			Text(
				text = contentColorName,
				modifier = Modifier.align(Alignment.Center),
				color = color,
				style = MaterialTheme.typography.bodySmall
			)
		}
	}
}
