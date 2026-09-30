/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

object SpermSliderDefaults {
	@Composable
	fun Track(
		colors: SliderColors = SliderDefaults.colors(),
		enabled: Boolean = true,
		sliderState: SliderState,
		gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
		stopSize: Dp = WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
		amplitude: (progress: Float) -> Float = WavyProgressIndicatorDefaults.indicatorAmplitude,
		wavelength: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
		waveSpeed: Dp = wavelength
	) {
		val color = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
		val trackColor =
			if (enabled) colors.inactiveTrackColor else colors.disabledInactiveTrackColor
		LinearWavyProgressIndicator(
			progress = { sliderState.coercedValueAsFraction },
			modifier = Modifier.fillMaxWidth(),
			gapSize = gapSize,
			stopSize = stopSize,
			amplitude = amplitude,
			wavelength = wavelength,
			waveSpeed = waveSpeed,
			color = color,
			trackColor = trackColor
		)
	}

	@Composable
	fun Thumb(
		interactionSource: MutableInteractionSource,
		colors: SliderColors = SliderDefaults.colors(),
		enabled: Boolean = true
	) {
		SliderDefaults.Thumb(
			interactionSource = interactionSource,
			colors = colors,
			enabled = enabled
		)
	}
}

@Composable
fun SpermSlider(
	state: SliderState,
	onValueChange: (Float) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	onValueChangeFinished: (() -> Unit)? = null,
	colors: SliderColors = SliderDefaults.colors(),
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	thumb: @Composable (SliderState) -> Unit = {
		SpermSliderDefaults.Thumb(
			interactionSource = interactionSource,
			colors = colors,
			enabled = enabled
		)
	},
	track: @Composable (SliderState) -> Unit = { sliderState ->
		SpermSliderDefaults.Track(
			colors = colors,
			enabled = enabled,
			sliderState = sliderState
		)
	},
) {
	Slider(
		state = state,
		onValueChange = onValueChange,
		modifier = modifier,
		enabled = enabled,
		onValueChangeFinished = onValueChangeFinished,
		colors = colors,
		interactionSource = interactionSource,
		thumb = thumb,
		track = track
	)
}
