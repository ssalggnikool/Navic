package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.components.common.SegmentedListItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsToggleItem(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	enabled: Boolean = true,
	shapes: ListItemShapes,
	contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
	isDividerShown: Boolean = false,
	supportingContent: @Composable (() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val interactionSource = remember { MutableInteractionSource() }

	SegmentedListItem(
		onClick = { onCheckedChange(!checked) },
		enabled = enabled,
		shapes = shapes,
		contentPadding = contentPadding,
		interactionSource = interactionSource,
		content = content,
		supportingContent = supportingContent,
		trailingContent = {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				if (preferenceManager.theme.isMaterialLike() && isDividerShown) {
					VerticalDivider(Modifier.height(32.dp).padding(horizontal = 14.dp))
				}
				Switch(
					modifier = Modifier.padding(start = 4.dp),
					checked = checked,
					onCheckedChange = null,
					enabled = enabled,
					interactionSource = interactionSource
				)
			}
		}
	)
}
