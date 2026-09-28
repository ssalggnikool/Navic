// this is literally just to use ContinuousCapsule for indicator shape :trol:

@file:Suppress("INVISIBLE_REFERENCE")

package paige.navic.ui.components.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AnimatedNavigationItem
import androidx.compose.material3.NavigationItemColors
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.WNRItemHorizontalPadding
import androidx.compose.material3.WNRItemNoLabelIndicatorPadding
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.tokens.NavigationRailHorizontalItemTokens
import androidx.compose.material3.tokens.NavigationRailVerticalItemTokens
import androidx.compose.material3.value
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kyant.capsule.ContinuousCapsule

@Composable
fun WideNavigationRailItem(
	selected: Boolean,
	onClick: () -> Unit,
	icon: @Composable () -> Unit,
	label: @Composable (() -> Unit)?,
	railExpanded: Boolean,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	iconPosition: NavigationItemIconPosition =
		WideNavigationRailItemDefaults.iconPositionFor(railExpanded),
	colors: NavigationItemColors = WideNavigationRailItemDefaults.colors(),
	interactionSource: MutableInteractionSource? = null,
	indicatorPadding: PaddingValues =
		WideNavigationRailItemDefaults.indicatorPadding(railExpanded = railExpanded),
) {
	@Suppress("NAME_SHADOWING")
	val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

	AnimatedNavigationItem(
		selected = selected,
		onClick = onClick,
		icon = icon,
		indicatorShape = ContinuousCapsule,
		topIconIndicatorWidth = NavigationRailVerticalItemTokens.ActiveIndicatorWidth,
		topIconLabelTextStyle = NavigationRailVerticalItemTokens.LabelTextFont.value,
		startIconLabelTextStyle = NavigationRailHorizontalItemTokens.LabelTextFont.value,
		indicatorPadding = indicatorPadding,
		topIconIndicatorToLabelVerticalPadding = NavigationRailVerticalItemTokens.IconLabelSpace,
		noLabelIndicatorPadding = WNRItemNoLabelIndicatorPadding,
		startIconToLabelHorizontalPadding = NavigationRailHorizontalItemTokens.IconLabelSpace,
		itemHorizontalPadding = WNRItemHorizontalPadding,
		colors = colors,
		modifier = modifier,
		enabled = enabled,
		label = label,
		iconPosition = iconPosition,
		interactionSource = interactionSource,
	)
}
