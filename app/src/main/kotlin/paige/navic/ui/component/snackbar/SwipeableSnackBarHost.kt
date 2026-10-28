package paige.navic.ui.component.snackbar

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeableSnackBarHost(
	hostState: SnackbarHostState,
	snackBar: @Composable (SnackbarData, Dp) -> Unit
) {
	val swipeThreshold = 100f
	val swipeScope = rememberCoroutineScope()
	val offset = remember { Animatable(0f) }

	LaunchedEffect(hostState.currentSnackbarData) {
		if (hostState.currentSnackbarData != null) {
			offset.snapTo(0f)
		}
	}

	fun handleSwipeRelease() {
		swipeScope.launch {
			if (abs(offset.value) > swipeThreshold) {
				hostState.currentSnackbarData?.dismiss()
				offset.animateTo(offset.value * 2)
			} else {
				offset.animateTo(0f)
			}
		}
	}

	SnackbarHost(
		hostState = hostState,
		modifier = Modifier.draggable(
			orientation = Orientation.Horizontal,
			state = rememberDraggableState { delta ->
				swipeScope.launch {
					offset.snapTo(offset.value + delta)
				}
			},
			onDragStopped = { handleSwipeRelease() }
		),
		snackbar = { data ->
			snackBar(data, with(LocalDensity.current) { offset.value.toDp() })
		}
	)
}
