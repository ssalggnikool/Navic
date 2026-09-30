/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.AudioGainManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.ReplayGainMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Info
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsNavItem
import paige.navic.ui.screen.settings.component.SettingsSliderItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem
import kotlin.math.absoluteValue
import kotlin.math.round

@Composable
fun AudioEffectsScreen() {
	val preferenceManager = koinInject<PreferenceManager>()
	val audioGainManager = koinInject<AudioGainManager>()
	val backStack = LocalNavStack.current

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(R.string.title_audio_effects)) }
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
				SettingsGroup(title = { Text(stringResource(R.string.title_playback)) }) {
					SettingsNavItem(
						onClick = dropUnlessResumed { backStack.add(Screen.Settings.Equalizer) },
						content = { Text(stringResource(R.string.option_equalizer)) },
						enabled = !preferenceManager.audioOffload,
						supportingContent = {
							Text(
								text = stringResource(
									if (!preferenceManager.audioOffload)
										R.string.subtitle_equalizer
									else R.string.subtitle_equalizer_disabled
								)
							)
						},
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 3)
					)
					SettingsToggleItem(
						checked = preferenceManager.gaplessPlayback,
						onCheckedChange = { preferenceManager.gaplessPlayback = it },
						content = { Text(stringResource(R.string.option_gapless_playback)) },
						supportingContent = { Text(stringResource(R.string.subtitle_gapless_playback)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 3)
					)
					SettingsToggleItem(
						checked = preferenceManager.audioOffload,
						onCheckedChange = { preferenceManager.audioOffload = it },
						content = { Text(stringResource(R.string.option_audio_offload)) },
						supportingContent = { Text(stringResource(R.string.subtitle_audio_offload)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 3)
					)
				}

				SettingsGroup(
					modifier = Modifier.selectableGroup(),
					title = { Text(stringResource(R.string.option_replaygain_mode)) }
				) {
					ReplayGainMode.entries.forEachIndexed { index, mode ->
						val selected = preferenceManager.replayGainMode == mode
						val interactionSource = remember { MutableInteractionSource() }

						SegmentedListItem(
							shapes = SegmentedListItemDefaults.segmentedShapes(
								index = index,
								count = ReplayGainMode.entries.count()
							),
							selected = selected,
							onClick = {
								preferenceManager.replayGainMode = mode
								audioGainManager.applyGainMode(mode)
							},
							interactionSource = interactionSource,
							leadingContent = {
								RadioButton(
									selected = selected,
									interactionSource = interactionSource,
									onClick = null
								)
							},
							content = { Text(stringResource(mode.displayName)) }
						)
					}
				}

				InformationTip(stringResource(R.string.option_dynamic_replaygain_tip))

				SettingsGroup(title = { Text(stringResource(R.string.option_title_preamp)) }) {
					SettingsSliderItem(
						content = { Text(stringResource(R.string.option_preamp_with_rg)) },
						trailingContent = { Text(preferenceManager.rgAmpGain.decibelsToHuman()) },
						value = preferenceManager.rgAmpGain,
						valueRange = -12f..12f,
						onValueChange = {
							preferenceManager.rgAmpGain = it.round(1)
							audioGainManager.setAmplifierValues(
								withReplayGain = it,
								withoutReplayGain = preferenceManager.ampGain
							)
						},
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 2)
					)
					SettingsSliderItem(
						content = { Text(stringResource(R.string.option_preamp_without_rg)) },
						trailingContent = { Text(preferenceManager.ampGain.decibelsToHuman()) },
						value = preferenceManager.ampGain,
						valueRange = -12f..12f,
						onValueChange = {
							preferenceManager.ampGain = it.round(1)
							audioGainManager.setAmplifierValues(
								withReplayGain = preferenceManager.rgAmpGain,
								withoutReplayGain = it
							)
						},
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 2)
					)
				}

				InformationTip(stringResource(R.string.option_preamp_tip))
			}
		}
	}
}


private fun Float.round(decimals: Int): Float {
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return (round(this * multiplier) / multiplier).toFloat()
}

private fun Float.decibelsToHuman(): String {
	val decibels = this.round(1)
	return buildString {
		if (decibels < 0) {
			append("-")
		} else if (decibels > 0) {
			append("+")
		} else {
			append(" ")
		}
		append("${decibels.absoluteValue}db")
	}
}

@Composable
private fun InformationTip(text: String) {
	Row(
		modifier = Modifier.padding(horizontal = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Icon(
			Icons.Outlined.Info,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = text,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}
