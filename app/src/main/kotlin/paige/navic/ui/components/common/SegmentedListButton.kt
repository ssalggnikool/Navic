package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedListButton(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource? = null,
	shapes: SegmentedListButtonShapes,
	colors: SegmentedListButtonColors = SegmentedListButtonDefaults.colors(),
	horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
		space = 4.dp,
		alignment = Alignment.CenterHorizontally
	),
	verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
	content: @Composable RowScope.() -> Unit
) {
	val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
	val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

	CompositionLocalProvider(LocalContentColor provides contentColor) {
		Box(
			modifier = modifier
				.clip(shapes.shape)
				.background(containerColor)
				.heightIn(min = SegmentedListButtonDefaults.ButtonHeight)
				.clickable(
					onClick = onClick,
					role = Role.Button,
					interactionSource = interactionSource
				)
		) {
			Row(
				modifier = Modifier.matchParentSize(),
				horizontalArrangement = horizontalArrangement,
				verticalAlignment = verticalAlignment,
				content = content
			)
		}
	}
}

data class SegmentedListButtonColors(
	val containerColor: Color,
	val contentColor: Color,
	val disabledContainerColor: Color,
	val disabledContentColor: Color
)

data class SegmentedListButtonShapes(
	val shape: Shape
)

object SegmentedListButtonDefaults {
	val ButtonHeight = 52.dp
	val SegmentedGap = 4.dp

	@Composable
	fun colors(
		containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
		contentColor: Color = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
		disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
	) = SegmentedListButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor
	)

	@Composable
	fun primaryColors() = SegmentedListButtonColors(
		containerColor = MaterialTheme.colorScheme.primary,
		contentColor = MaterialTheme.colorScheme.onPrimary,
		disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
		disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
	)

	@Composable
	fun errorColors() = SegmentedListButtonColors(
		containerColor = MaterialTheme.colorScheme.error,
		contentColor = MaterialTheme.colorScheme.onError,
		disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
		disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)
	)

	@Composable
	fun shapes(
		index: Int,
		count: Int,
		baseShape: CornerBasedShape = MaterialTheme.shapes.extraSmall,
		overrideShape: CornerBasedShape = MaterialTheme.shapes.large
	): SegmentedListButtonShapes {
		return remember {
			val shape = when {
				// only item
				count == 1 -> overrideShape

				// top
				index == 0 -> baseShape.copy(
					topStart = overrideShape.topStart,
					topEnd = overrideShape.topEnd
				)

				// bottom
				index == count - 1 -> baseShape.copy(
					bottomStart = overrideShape.bottomStart,
					bottomEnd = overrideShape.bottomEnd
				)

				else -> overrideShape
			}

			SegmentedListButtonShapes(shape = shape)
		}
	}
}
