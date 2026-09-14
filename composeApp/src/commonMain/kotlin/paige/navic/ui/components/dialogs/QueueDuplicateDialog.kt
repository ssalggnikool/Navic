package paige.navic.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_dont_show_again
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.notice_queue_duplicate
import navic.composeapp.generated.resources.title_confirm
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults

@Composable
fun QueueDuplicateDialog(
	onDismissRequest: () -> Unit,
	onConfirm: () -> Unit
) {
	val preferenceManager = koinInject<PreferenceManager>()

	FormDialog(
		onDismissRequest = onDismissRequest,
		icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
		title = { Text(stringResource(Res.string.title_confirm)) },
		contentGap = 0.dp,
		verticalArrangement = Arrangement.spacedBy(0.dp),
		content = {
			Text(stringResource(Res.string.notice_queue_duplicate))

			val interactionSource = remember { MutableInteractionSource() }
			Row(
				modifier = Modifier
					.clickable(
						interactionSource = interactionSource,
						onClick = {
							preferenceManager.shushQueueDuplicateDialog =
								!preferenceManager.shushQueueDuplicateDialog
						},
						role = Role.Checkbox
					)
					.fillMaxWidth()
					.minimumInteractiveComponentSize()
					.clip(MaterialTheme.shapes.medium),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = stringResource(Res.string.action_dont_show_again),
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Checkbox(
					checked = preferenceManager.shushQueueDuplicateDialog,
					interactionSource = interactionSource,
					onCheckedChange = null
				)
			}
		},
		buttons = {
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					onConfirm()
					onDismissRequest()
				},
				shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 2)
			) {
				Text(stringResource(Res.string.action_ok))
			}
			SegmentedListButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = onDismissRequest,
				shapes = SegmentedListButtonDefaults.shapes(index = 1, count = 2)
			) {
				Text(stringResource(Res.string.action_cancel))
			}
		},
	)
}
