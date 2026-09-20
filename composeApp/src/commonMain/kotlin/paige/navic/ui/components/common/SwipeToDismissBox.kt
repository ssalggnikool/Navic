package paige.navic.ui.components.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * `SwipeToDismissBox` but it has haptics.
 */
@Composable
fun SwipeToDismissBox(
	state: SwipeToDismissBoxState,
	backgroundContent: @Composable RowScope.() -> Unit,
	modifier: Modifier = Modifier,
	enableDismissFromStartToEnd: Boolean = true,
	enableDismissFromEndToStart: Boolean = true,
	gesturesEnabled: Boolean = true,
	onDismiss: (SwipeToDismissBoxValue) -> Unit = {},
	content: @Composable RowScope.() -> Unit,
) {
	val haptics = LocalHapticFeedback.current

	LaunchedEffect(state.targetValue) {
		if (state.targetValue != SwipeToDismissBoxValue.Settled) {
			haptics.performHapticFeedback(HapticFeedbackType.LongPress)
		}
	}

	androidx.compose.material3.SwipeToDismissBox(
		state = state,
		backgroundContent = backgroundContent,
		modifier = modifier,
		enableDismissFromStartToEnd = enableDismissFromStartToEnd,
		enableDismissFromEndToStart = enableDismissFromEndToStart,
		gesturesEnabled = gesturesEnabled,
		onDismiss = onDismiss,
		content = content
	)
}
