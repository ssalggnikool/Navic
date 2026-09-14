@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package paige.navic.ui.components.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle

@Composable
fun SegmentedListItem(
	onClick: () -> Unit,
	shapes: ListItemShapes,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	leadingContent: @Composable (() -> Unit)? = null,
	trailingContent: @Composable (() -> Unit)? = null,
	overlineContent: @Composable (() -> Unit)? = null,
	supportingContent: @Composable (() -> Unit)? = null,
	verticalAlignment: Alignment.Vertical = SegmentedListItemDefaults.verticalAlignment(),
	onLongClick: (() -> Unit)? = null,
	onLongClickLabel: String? = null,
	colors: ListItemColors = SegmentedListItemDefaults.segmentedColors(),
	elevation: ListItemElevation = ListItemDefaults.elevation(),
	contentPadding: PaddingValues = SegmentedListItemDefaults.ContentPadding,
	interactionSource: MutableInteractionSource? = null,
	content: @Composable () -> Unit,
) {
	androidx.compose.material3.SegmentedListItem(
		modifier = modifier,
		content = content,
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		overlineContent = overlineContent,
		supportingContent = supportingContent,
		verticalAlignment = verticalAlignment,
		enabled = enabled,
		onClick = onClick,
		onLongClick = onLongClick,
		onLongClickLabel = onLongClickLabel,
		interactionSource = interactionSource,
		colors = colors,
		shapes = shapes,
		elevation = elevation,
		contentPadding = contentPadding,
	)
}

@Composable
fun SegmentedListItem(
	selected: Boolean,
	onClick: () -> Unit,
	shapes: ListItemShapes,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	leadingContent: @Composable (() -> Unit)? = null,
	trailingContent: @Composable (() -> Unit)? = null,
	overlineContent: @Composable (() -> Unit)? = null,
	supportingContent: @Composable (() -> Unit)? = null,
	verticalAlignment: Alignment.Vertical = SegmentedListItemDefaults.verticalAlignment(),
	onLongClick: (() -> Unit)? = null,
	onLongClickLabel: String? = null,
	colors: ListItemColors = SegmentedListItemDefaults.segmentedColors(),
	elevation: ListItemElevation = ListItemDefaults.elevation(),
	contentPadding: PaddingValues = SegmentedListItemDefaults.ContentPadding,
	interactionSource: MutableInteractionSource? = null,
	content: @Composable () -> Unit,
) {
	val colors = if (selected) {
		colors.copy(
			containerColor = colors.selectedContainerColor,
			contentColor = colors.selectedContentColor,
			leadingContentColor = colors.selectedLeadingContentColor,
			trailingContentColor = colors.selectedTrailingContentColor,
			overlineContentColor = colors.selectedOverlineContentColor,
			supportingContentColor = colors.selectedSupportingContentColor
		)
	} else {
		colors
	}

	androidx.compose.material3.SegmentedListItem(
		modifier = modifier.semantics {
			this@semantics.selected = selected
		},
		content = content,
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		overlineContent = overlineContent,
		supportingContent = supportingContent,
		verticalAlignment = verticalAlignment,
		enabled = enabled,
		selected = false,
		onClick = onClick,
		onLongClick = onLongClick,
		onLongClickLabel = onLongClickLabel,
		interactionSource = interactionSource,
		colors = colors,
		shapes = shapes,
		elevation = elevation,
		contentPadding = contentPadding,
	)
}

object SegmentedListItemDefaults {
	val SegmentedGap = 3.dp
	val ContentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
	val OverrideShape = ContinuousRoundedRectangle(18.dp)

	@Composable
	fun verticalAlignment() = Alignment.CenterVertically

	@Composable
	fun segmentedColors(
		// default
		containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
		contentColor: Color = MaterialTheme.colorScheme.onSurface,
		leadingContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		trailingContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		overlineContentColor: Color = MaterialTheme.colorScheme.primary,
		supportingContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,

		// disabled
		disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
		disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		disabledLeadingContentColor: Color = leadingContentColor,
		disabledTrailingContentColor: Color = trailingContentColor,
		disabledOverlineContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		disabledSupportingContentColor: Color = supportingContentColor,

		// selected
		selectedContainerColor: Color = containerColor,
		selectedContentColor: Color = contentColor,
		selectedLeadingContentColor: Color = leadingContentColor,
		selectedTrailingContentColor: Color = trailingContentColor,
		selectedOverlineContentColor: Color = overlineContentColor,
		selectedSupportingContentColor: Color = supportingContentColor,

		// dragged
		draggedContainerColor: Color = containerColor,
		draggedContentColor: Color = contentColor,
		draggedLeadingContentColor: Color = leadingContentColor,
		draggedTrailingContentColor: Color = trailingContentColor,
		draggedOverlineContentColor: Color = overlineContentColor,
		draggedSupportingContentColor: Color = supportingContentColor
	) = ListItemDefaults.segmentedColors(
		// default
		containerColor = containerColor,
		contentColor = contentColor,
		leadingContentColor = leadingContentColor,
		trailingContentColor = trailingContentColor,
		overlineContentColor = overlineContentColor,
		supportingContentColor = supportingContentColor,

		// disabled
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledLeadingContentColor = disabledLeadingContentColor,
		disabledTrailingContentColor = disabledTrailingContentColor,
		disabledOverlineContentColor = disabledOverlineContentColor,
		disabledSupportingContentColor = disabledSupportingContentColor,

		// selected
		selectedContainerColor = selectedContainerColor,
		selectedContentColor = selectedContentColor,
		selectedLeadingContentColor = selectedLeadingContentColor,
		selectedTrailingContentColor = selectedTrailingContentColor,
		selectedOverlineContentColor = selectedOverlineContentColor,
		selectedSupportingContentColor = selectedSupportingContentColor,

		// dragged
		draggedContainerColor = draggedContainerColor,
		draggedContentColor = draggedContentColor,
		draggedLeadingContentColor = draggedLeadingContentColor,
		draggedTrailingContentColor = draggedTrailingContentColor,
		draggedOverlineContentColor = draggedOverlineContentColor,
		draggedSupportingContentColor = draggedSupportingContentColor
	)

	@Composable
	fun segmentedErrorColors() = segmentedColors(
		contentColor = MaterialTheme.colorScheme.error,
		supportingContentColor = MaterialTheme.colorScheme.error
	)

	@Composable
	fun segmentedShapes(
		index: Int,
		count: Int,
		defaultShapes: ListItemShapes = ListItemDefaults.shapes(),
		dismissDirection: SwipeToDismissBoxValue? = null
	): ListItemShapes {
		return remember(index, count, defaultShapes, dismissDirection) {
			when {
				count == 1 || (dismissDirection != SwipeToDismissBoxValue.Settled && dismissDirection != null) -> {
					defaultShapes.copy(
						shape = OverrideShape
					)
				}

				index == 0 -> {
					val defaultBaseShape = defaultShapes.shape
					if (defaultBaseShape is CornerBasedShape) {
						defaultShapes.copy(
							shape =
								defaultBaseShape.copy(
									topStart = OverrideShape.topStart,
									topEnd = OverrideShape.topEnd,
								)
						)
					} else {
						defaultShapes
					}
				}

				index == count - 1 -> {
					val defaultBaseShape = defaultShapes.shape
					if (defaultBaseShape is CornerBasedShape) {
						defaultShapes.copy(
							shape =
								defaultBaseShape.copy(
									bottomStart = OverrideShape.bottomStart,
									bottomEnd = OverrideShape.bottomEnd,
								)
						)
					} else {
						defaultShapes
					}
				}

				else -> defaultShapes
			}
		}
	}
}
