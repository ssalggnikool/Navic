package paige.navic.ui.components.snackbars

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_error_show
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import paige.navic.di.LocalSnackBarState
import paige.navic.ui.components.common.ErrorCodeBlock
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.util.Logger

@Composable
fun ErrorSnackBar(
	error: Throwable?,
	onClearError: () -> Unit
) {
	if (error == null) return

	val snackBarState = LocalSnackBarState.current
	var visible by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(error) {
		val result = snackBarState.showSnackbar(
			message = getString(Res.string.info_error),
			actionLabel = getString(Res.string.info_error_show),
			duration = SnackbarDuration.Long
		)
		if (result == SnackbarResult.ActionPerformed) {
			visible = true
			Logger.e("ErrorSnackBar", "Printing stack trace for error", error)
		} else {
			onClearError()
		}
	}

	if (!visible) return

	FormDialog(
		onDismissRequest = {
			visible = false
			onClearError()
		},
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					visible = false
					onClearError()
				},
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 1)
			) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	) {
		ErrorCodeBlock(error)
	}
}
