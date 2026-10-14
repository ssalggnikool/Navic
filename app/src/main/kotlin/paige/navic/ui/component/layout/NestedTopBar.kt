/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.ArrowBack


object NestedTopBarDefaults {
	@Composable
	fun NavigationAction(
		colors: NestedTopBarButtonColors = NestedTopBarButtonDefaults.colors()
	) {
		val backStack = LocalNavStack.current
		TopBarButton(
			modifier = Modifier.padding(start = 20.dp, end = 13.dp),
			onClick = dropUnlessResumed {
				if (backStack.size > 1) {
					backStack.removeLastOrNull()
				}
			},
			colors = colors
		) {
			Icon(
				imageVector = Icons.Outlined.ArrowBack,
				contentDescription = stringResource(R.string.action_navigate_back)
			)
		}
	}
}

@Composable
fun NestedTopBar(
	title: @Composable () -> Unit,
	actions: @Composable RowScope.() -> Unit = {},
	navigationAction: @Composable () -> Unit = NestedTopBarDefaults::NavigationAction,
	colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
	TopAppBar(
		title = title,
		colors = colors,
		actions = {
			Row(
				modifier = Modifier.padding(end = 20.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
				content = actions
			)
		},
		navigationIcon = navigationAction,
	)
}

data class NestedTopBarButtonColors(
	val containerColor: Color,
	val contentColor: Color,
	val disabledContainerColor: Color,
	val disabledContentColor: Color
)

object NestedTopBarButtonDefaults {
	@Composable
	fun colors(
		containerColor: Color = Color.Unspecified,
		contentColor: Color = Color.Unspecified,
		disabledContainerColor: Color = Color.Unspecified,
		disabledContentColor: Color = Color.Unspecified
	) = NestedTopBarButtonColors(
		containerColor = containerColor.takeOrElse { MaterialTheme.colorScheme.surfaceContainer },
		contentColor = contentColor.takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant },
		disabledContainerColor = disabledContainerColor.takeOrElse { MaterialTheme.colorScheme.surfaceContainerLow },
		disabledContentColor = disabledContentColor
			.takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f) }
	)
}

@Composable
fun TopBarButton(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	enabled: Boolean = true,
	colors: NestedTopBarButtonColors = NestedTopBarButtonDefaults.colors(),
	shape: Shape = CircleShape,
	shadowElevation: Dp = 0.dp,
	content: @Composable BoxScope.() -> Unit
) {
	Surface(
		modifier = modifier.size(40.dp),
		onClick = onClick,
		enabled = enabled,
		shape = shape,
		shadowElevation = shadowElevation,
		color = if (enabled) colors.containerColor else colors.disabledContainerColor,
		contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
	) {
		Box(contentAlignment = Alignment.Center) {
			Box(
				modifier = Modifier.size(24.dp),
				content = content
			)
		}
	}
}
