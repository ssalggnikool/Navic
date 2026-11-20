/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun DurationPicker(
	duration: Duration,
	onDurationChange: (Duration) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	val totalMinutes = duration.inWholeMinutes
	val hours = totalMinutes / 60
	val minutes = totalMinutes % 60

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		TimeCard(
			value = hours,
			onValueChange = { newHours ->
				onDurationChange(newHours.hours + minutes.minutes)
			},
			enabled = enabled
		)

		Text(
			text = ":",
			style = MaterialTheme.typography.headlineLarge,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(horizontal = 8.dp)
		)

		TimeCard(
			value = minutes,
			onValueChange = { newMinutes ->
				onDurationChange(hours.hours + newMinutes.minutes)
			},
			isMinutes = true,
			enabled = enabled
		)
	}
}

@Composable
private fun TimeCard(
	value: Long,
	onValueChange: (Long) -> Unit,
	isMinutes: Boolean = false,
	enabled: Boolean
) {
	val formatted = value.toString().padStart(2, '0')
	val state = rememberTextFieldState(formatted)
	val currentOnValueChange by rememberUpdatedState(onValueChange)

	LaunchedEffect(formatted) {
		if (state.text.toString() != formatted) {
			state.setTextAndPlaceCursorAtEnd(formatted)
		}
	}

	LaunchedEffect(state) {
		snapshotFlow { state.text.toString() }
			.drop(1)
			.collect { newText ->
				currentOnValueChange(newText.toLongOrNull() ?: 0L)
			}
	}

	val inputTransformation = remember(isMinutes) {
		InputTransformation {
			val numericString = asCharSequence().toString().filter { it.isDigit() }
			val newText = if (numericString.length > 2) {
				numericString.takeLast(2)
			} else {
				numericString.padStart(2, '0')
			}
			val newValue = newText.toLongOrNull() ?: 0L

			if (isMinutes && newValue > 59) {
				revertAllChanges()
			} else {
				replace(0, length, newText)
			}
		}
	}

	val focus = remember { FocusRequester() }
	var isFocused by remember { mutableStateOf(false) }

	val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
	val backgroundColor = if (isFocused) {
		MaterialTheme.colorScheme.primaryContainer
	} else {
		MaterialTheme.colorScheme.surfaceVariant
	}

	BasicTextField(
		state = state,
		enabled = enabled,
		inputTransformation = inputTransformation,
		textStyle = MaterialTheme.typography.headlineLarge.copy(
			color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			fontSize = 50.sp
		),
		keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
		lineLimits = TextFieldLineLimits.SingleLine,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
		decorator = { innerTextField ->
			Box(
				modifier = Modifier
					.width(120.dp)
					.background(backgroundColor, MaterialTheme.shapes.medium)
					.border(2.dp, borderColor, MaterialTheme.shapes.medium)
					.padding(vertical = 8.dp),
				contentAlignment = Alignment.Center
			) {
				innerTextField()
			}
		},
		modifier = Modifier
			.focusRequester(focus)
			.onFocusChanged { isFocused = it.isFocused },
	)
}
