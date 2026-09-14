package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.components.common.SegmentedListItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <Choice> SettingsChoiceItem(
	choices: ImmutableList<Choice>,
	selectedChoice: Choice,
	onChoiceSelected: (Choice) -> Unit,
	label: @Composable (Choice) -> String,
	description: String? = null,
	shapes: ListItemShapes,
	content: @Composable () -> Unit
) {
	var choiceDialogOpen by rememberSaveable { mutableStateOf(false) }

	SegmentedListItem(
		shapes = shapes,
		onClick = { choiceDialogOpen = true },
		content = content,
		supportingContent = {
			Text(buildString {
				append(label(selectedChoice))
				description?.let { description ->
					append(" // $description")
				}
			})
		}
	)

	if (choiceDialogOpen) {
		ChoiceDialog(
			onDismissRequest = { choiceDialogOpen = false },
			choices = choices,
			selectedChoice = selectedChoice,
			onChoiceSelected = onChoiceSelected,
			label = label,
			title = content
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <Choice> ChoiceDialog(
	onDismissRequest: () -> Unit,
	choices: ImmutableList<Choice>,
	selectedChoice: Choice,
	onChoiceSelected: (Choice) -> Unit,
	label: @Composable (Choice) -> String,
	title: @Composable () -> Unit
) {
	val listItemColors = ListItemDefaults.colors(
		containerColor = AlertDialogDefaults.containerColor,
		contentColor = AlertDialogDefaults.titleContentColor,
		selectedContainerColor = AlertDialogDefaults.containerColor,
		selectedContentColor = AlertDialogDefaults.titleContentColor
	)
	val listItemContentPadding = PaddingValues(
		horizontal = 4.dp,
		vertical = 10.dp
	)

	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = title,
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.selectableGroup()
			) {
				choices.forEach { choice ->
					val selected = selectedChoice == choice
					ListItem(
						selected = selected,
						onClick = { onChoiceSelected(choice) },
						content = { Text(label(choice)) },
						colors = listItemColors,
						contentPadding = listItemContentPadding,
						trailingContent = {
							RadioButton(
								selected = selected,
								onClick = null
							)
						}
					)
				}
			}
		},
		confirmButton = {
			Button(onClick = onDismissRequest) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	)
}
