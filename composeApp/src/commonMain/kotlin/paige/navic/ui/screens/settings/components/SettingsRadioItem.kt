package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import paige.navic.ui.components.common.SegmentedListItem


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsRadioItem(
	selected: Boolean,
	onClick: () -> Unit,
	enabled: Boolean = true,
	shapes: ListItemShapes,
	contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	SegmentedListItem(
		onClick = onClick,
		enabled = enabled,
		selected = selected,
		shapes = shapes,
		contentPadding = contentPadding,
		content = content,
		supportingContent = supportingContent,
		leadingContent = {
			RadioButton(
				selected = selected,
				onClick = null
			)
		}
	)
}
