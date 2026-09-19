package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_allow
import navic.composeapp.generated.resources.dialog_notification_permission_message
import navic.composeapp.generated.resources.dialog_notification_permission_title
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults

@Composable
fun NotificationPermissionDialog(
	onDismiss: () -> Unit,
	onAllow: () -> Unit
) {
	FormDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(Res.string.dialog_notification_permission_title)) },
		content = { Text(stringResource(Res.string.dialog_notification_permission_message)) },
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = onAllow,
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 1)
			) {
				Text(stringResource(Res.string.action_allow))
			}
		}
	)
}
