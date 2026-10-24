/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.StreamingQuality
import paige.navic.domain.model.settings.description
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Info
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsRadioItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem

@Composable
fun SettingsDownloadQualityScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.title_download_quality)) }) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp)
			) {
				AnimatedVisibility(visible = !preferenceManager.isAdvancedDownloadTranscodingActive) {
					SettingsGroup(
						modifier = Modifier
							.selectableGroup()
							.padding(bottom = SettingsGroupDefaults.GapBetweenGroups),
					) {
						RadioButtons(
							value = preferenceManager.downloadQuality,
							onChangeValue = { preferenceManager.downloadQuality = it }
						)
					}
				}

				SettingsGroup(
					modifier = Modifier.padding(bottom = SettingsGroupDefaults.GapBetweenGroups),
					title = { Text(stringResource(R.string.title_advanced)) }
				) {
					SettingsToggleItem(
						checked = preferenceManager.isAdvancedDownloadTranscodingActive,
						onCheckedChange = {
							preferenceManager.isAdvancedDownloadTranscodingActive = it
						},
						content = { Text(stringResource(R.string.option_download_custom_quality)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1)
					)
				}

				AnimatedVisibility(visible = preferenceManager.isAdvancedDownloadTranscodingActive) {
					CustomOptions()
				}

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
						stringResource(R.string.info_download_quality),
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@Composable
private fun RadioButtons(
	value: StreamingQuality,
	onChangeValue: (StreamingQuality) -> Unit
) {
	StreamingQuality.entries.forEachIndexed { index, quality ->
		val selected = value == quality

		SettingsRadioItem(
			selected = selected,
			onClick = { onChangeValue(quality) },
			content = { Text(stringResource(quality.displayName)) },
			supportingContent = {
				quality.description()?.let { description ->
					AnimatedVisibility(visible = selected) {
						Text(description)
					}
				}
			},
			shapes = SegmentedListItemDefaults.segmentedShapes(
				index = index,
				count = StreamingQuality.entries.count()
			)
		)
	}
}

@Composable
private fun CustomOptions() {
	val preferenceManager = koinInject<PreferenceManager>()
	var wifiInput by remember {
		val current = preferenceManager.customDownloadMaxBitrate
		mutableStateOf(if (current > 0) current.toString() else "")
	}

	Column(
		modifier = Modifier.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		OutlinedTextField(
			value = wifiInput,
			onValueChange = { newValue ->
				if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
					wifiInput = newValue
					preferenceManager.customDownloadMaxBitrate =
						newValue.toIntOrNull() ?: 0
				}
			},
			label = { Text(stringResource(R.string.option_max_bitrate)) },
			placeholder = { Text("0") },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
			modifier = Modifier.fillMaxWidth(),
			singleLine = true
		)

		OutlinedTextField(
			value = preferenceManager.customDownloadFormat,
			onValueChange = { preferenceManager.customDownloadFormat = it },
			label = { Text(stringResource(R.string.option_download_format)) },
			modifier = Modifier.fillMaxWidth(),
			singleLine = true
		)
	}
}
