package paige.navic.ui.component.dialog

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import paige.navic.ui.navigation.PredictiveBackState
import paige.navic.ui.navigation.rememberPredictiveBackState

@Composable
fun AlertDialog(
	onDismissRequest: () -> Unit,
	confirmButton: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	dismissButton: @Composable (() -> Unit)? = null,
	icon: @Composable (() -> Unit)? = null,
	title: @Composable (() -> Unit)? = null,
	text: @Composable (() -> Unit)? = null,
	shape: Shape = AlertDialogDefaults.shape,
	containerColor: Color = AlertDialogDefaults.containerColor,
	iconContentColor: Color = AlertDialogDefaults.iconContentColor,
	titleContentColor: Color = AlertDialogDefaults.titleContentColor,
	textContentColor: Color = AlertDialogDefaults.textContentColor,
	tonalElevation: Dp = AlertDialogDefaults.TonalElevation
) {
	var predictiveBackState by remember { mutableStateOf<PredictiveBackState?>(null) }

	androidx.compose.material3.AlertDialog(
		onDismissRequest = onDismissRequest,
		confirmButton = confirmButton,
		modifier = modifier
			.graphicsLayer {
				predictiveBackState?.let { predictiveBackState ->
					translationX = predictiveBackState.offsetX
					translationY = predictiveBackState.offsetY
					scaleX = predictiveBackState.scale
					scaleY = predictiveBackState.scale
				}
			},
		dismissButton = dismissButton,
		icon = icon,
		title = title,
		text = {
			predictiveBackState = rememberPredictiveBackState(
				onBackCompleted = onDismissRequest
			)
			text?.invoke()
		},
		shape = shape,
		containerColor = containerColor,
		iconContentColor = iconContentColor,
		titleContentColor = titleContentColor,
		textContentColor = textContentColor,
		tonalElevation = tonalElevation,
		properties = DialogProperties(
			dismissOnBackPress = false
		),
	)
}
