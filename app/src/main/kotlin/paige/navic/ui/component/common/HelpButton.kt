package paige.navic.ui.component.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Help

@Composable
fun HelpButton(
	modifier: Modifier = Modifier,
	helpDescription: @Composable () -> Unit
) {
	var dialogIsOpen by rememberSaveable { mutableStateOf(false) }
	val onDismissRequest = { dialogIsOpen = false }

	IconButton(
		modifier = modifier,
		onClick = { dialogIsOpen = true }
	) {
		Icon(
			imageVector = Icons.Outlined.Help,
			contentDescription = stringResource(R.string.action_help)
		)
	}

	if (dialogIsOpen) {
		AlertDialog(
			onDismissRequest = onDismissRequest,
			text = helpDescription,
			confirmButton = {
				Button(onClick = onDismissRequest) {
					Text(stringResource(R.string.action_ok))
				}
			}
		)
	}
}
