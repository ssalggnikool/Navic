package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paige.navic.ui.components.common.SegmentedListItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSliderItem(
	value: Float,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	onValueChange: (Float) -> Unit,
	steps: Int = 0,
	enabled: Boolean = true,
	shapes: ListItemShapes,
	contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
	trailingContent: @Composable () -> Unit,
	content: @Composable () -> Unit
) {
	SegmentedListItem(
		onClick = {},
		enabled = enabled,
		shapes = shapes,
		contentPadding = contentPadding,
		content = content,
		supportingContent = {
			Slider(
				value = value,
				valueRange = valueRange,
				onValueChange = onValueChange,
				steps = steps
			)
		},
		trailingContent = {
			CompositionLocalProvider(
				LocalTextStyle provides TextStyle(
					fontFamily = FontFamily.Monospace,
					fontWeight = FontWeight(400),
					fontSize = 13.sp
				)
			) {
				trailingContent()
			}
		}
	)
}
