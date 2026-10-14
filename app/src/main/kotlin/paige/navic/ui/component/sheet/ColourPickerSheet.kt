/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.materialkolor.ktx.toHex
import dev.zt64.compose.pipette.HsvColor
import dev.zt64.compose.pipette.SquareColorPicker
import kotlinx.coroutines.launch
import paige.navic.R
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Copy
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
	onDismissRequest: () -> Unit,
	color: HsvColor,
	onColorChange: (HsvColor) -> Unit
) {
	val hex = remember(color) {
		color.toColor().toHex()
	}
	val clipboard = LocalClipboardManager.current
	val sheetState = rememberBottomSheetState(SheetValue.Hidden)
	val closeScope = rememberCoroutineScope()
	val animateToDismiss: () -> Unit = {
		closeScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			if (!sheetState.isVisible) {
				onDismissRequest()
			}
		}
	}

	ModalBottomSheet(
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Surface {
			Column(
				modifier = Modifier.padding(12.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				OutlinedTextField(
					modifier = Modifier.fillMaxWidth(),
					value = hex,
					onValueChange = {
						runCatching {
							val color = HsvColor(it.removePrefix("#").toLong(16))
							onColorChange(color)
						}
					},
					trailingIcon = {
						// why
						Box(
							modifier = Modifier.width(55.dp),
							contentAlignment = Alignment.Center
						) {
							IconButton(
								onClick = { clipboard.setText(AnnotatedString(hex)) }
							) {
								Icon(
									imageVector = Icons.Outlined.Copy,
									contentDescription = stringResource(R.string.action_copy)
								)
							}
						}
					}
				)
				Row(
					modifier = Modifier.fillMaxWidth().height(150.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
						SquareColorPicker(
							color = { color },
							onColorChange = onColorChange,
							shape = MaterialTheme.shapes.extraSmall,
							modifier = Modifier.fillMaxSize()
						)
					}
					HueSlider(
						modifier = Modifier.fillMaxHeight(),
						hue = color.hue,
						onHueChange = { onColorChange(color.copy(hue = it)) }
					)
				}
			}
		}

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.End
		) {
			Button(onClick = animateToDismiss) {
				Text(stringResource(R.string.action_ok))
			}
		}
	}
}

@Composable
private fun HueSlider(
	modifier: Modifier = Modifier,
	hue: Float,
	onHueChange: (Float) -> Unit
) {
	var sliderHeight by remember { mutableFloatStateOf(0f) }

	Box(
		modifier = modifier
			.width(44.dp)
			.onGloballyPositioned { coordinates ->
				sliderHeight = coordinates.size.height.toFloat()
			}
			.pointerInput(Unit) {
				detectTapGestures { offset ->
					if (sliderHeight > 0) {
						val newValue = (offset.y / sliderHeight).coerceIn(0f, 1f) * 360f
						onHueChange(newValue)
					}
				}
			}
			.pointerInput(Unit) {
				detectDragGestures { change, _ ->
					change.consume()
					if (sliderHeight > 0) {
						val newValue = (change.position.y / sliderHeight).coerceIn(0f, 1f) * 360f
						onHueChange(newValue)
					}
				}
			},
		contentAlignment = Alignment.TopCenter
	) {
		Box(
			modifier = Modifier
				.matchParentSize()
				.background(
					brush = Brush.verticalGradient(
						listOf(
							Color.Red,
							Color.Yellow,
							Color.Green,
							Color.Cyan,
							Color.Blue,
							Color.Magenta,
							Color.Red
						)
					),
					shape = MaterialTheme.shapes.extraSmall
				)
		)

		if (sliderHeight > 0) {
			val thumbHeight = 8.dp
			val thumbYPx = (hue / 360f) * sliderHeight

			Box(
				modifier = Modifier
					.offset {
						IntOffset(
							x = 0,
							y = (thumbYPx - thumbHeight.toPx()).roundToInt()
						)
					}
					.fillMaxWidth()
					.height(thumbHeight * 2)
					.background(Color.White, ContinuousCapsule)
					.border(2.dp, Color.LightGray, ContinuousCapsule)
			)
		}
	}
}
