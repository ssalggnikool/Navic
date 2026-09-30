/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.NowPlayingSliderStyle
import paige.navic.ui.component.common.SlimSlider
import paige.navic.ui.component.common.SpermSlider
import paige.navic.ui.component.common.SpermSliderDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSliderStyleDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val sliderState = rememberSliderState(value = 0.6767f)
	val onValueChange: (Float) -> Unit = { value ->
		sliderState.value = value
	}

	val preferenceManager = koinInject<PreferenceManager>()
	val interactionSource = remember { MutableInteractionSource() }

	AlertDialog(
		title = {
			Text(stringResource(R.string.option_now_playing_slider_style))
		},
		text = {
			LazyVerticalGrid(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				columns = GridCells.Fixed(2),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				NowPlayingSliderStyle.entries.forEach { style ->
					item(key = style.ordinal) {
						Option(
							onClick = {
								preferenceManager.nowPlayingSliderStyle = style
							},
							selected = preferenceManager.nowPlayingSliderStyle == style,
							label = stringResource(style.displayName)
						) {
							when (style) {
								NowPlayingSliderStyle.Flat -> {
									Slider(
										state = sliderState,
										onValueChange = onValueChange,
										interactionSource = interactionSource,
										modifier = Modifier.requiredWidth(200.dp).scale(.5f)
									)
								}

								NowPlayingSliderStyle.Squiggly, NowPlayingSliderStyle.Yoyo -> {
									val isYoyo = style == NowPlayingSliderStyle.Yoyo
									SpermSlider(
										state = sliderState,
										onValueChange = onValueChange,
										modifier = Modifier.requiredWidth(200.dp).scale(.5f),
										thumb = {
											SliderDefaults.Thumb(
												thumbSize = if (isYoyo) DpSize(
													20.dp,
													20.dp
												) else DpSize(4.dp, 32.dp),
												interactionSource = remember { MutableInteractionSource() }
											)
										},
										track = { sliderState ->
											SpermSliderDefaults.Track(
												sliderState = sliderState,
												gapSize = if (isYoyo) 0.dp else 6.dp,
												wavelength = if (isYoyo) 32.dp else 26.dp,
												amplitude = { 1f }
											)
										}
									)
								}

								NowPlayingSliderStyle.Slim -> {
									SlimSlider(
										state = sliderState,
										onValueChange = onValueChange,
										modifier = Modifier.requiredWidth(200.dp).scale(.5f)
									)
								}
							}
						}
					}
				}
			}
		},
		onDismissRequest = onDismissRequest,
		confirmButton = {
			Button(onClick = {
				onDismissRequest()
			}) {
				Text(stringResource(R.string.action_ok))
			}
		}
	)
}

@Composable
private fun Option(
	onClick: () -> Unit,
	selected: Boolean,
	label: String,
	content: @Composable () -> Unit
) {
	Card(
		border = BorderStroke(
			width = 1.dp,
			color = if (selected)
				MaterialTheme.colorScheme.primary
			else MaterialTheme.colorScheme.outlineVariant
		),
		shape = MaterialTheme.shapes.large,
		onClick = {
			onClick()
		}
	) {
		Column {
			Box(
				modifier = Modifier
					.padding(8.dp)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				content()
			}
			Box(
				modifier = Modifier
					.padding(12.dp)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				Text(label)
			}
		}
	}
}
