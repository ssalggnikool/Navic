package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import paige.navic.R
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Download
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults

@Composable
fun BulkDownloadDialog(
	title: String,
	message: String,
	showDialog: Boolean,
	onDismissRequest: () -> Unit,
	onConfirm: () -> Unit
) {
	if (showDialog) {
		FormDialog(
			onDismissRequest = onDismissRequest,
			icon = { Icon(Icons.Outlined.Download, null) },
			title = { Text(title) },
			content = { Text(message) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						onConfirm()
						onDismissRequest()
					},
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2),
					colors = SegmentedListButtonDefaults.primaryColors()
				) {
					Text(stringResource(R.string.action_download))
				}
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = onDismissRequest,
					shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
				) {
					Text(stringResource(R.string.action_cancel))
				}
			}
		)
	}
}
