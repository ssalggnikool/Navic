package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.theme.defaultFont

@Composable
fun SettingsGroup(
	modifier: Modifier = Modifier,
	title: (@Composable () -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(modifier = modifier.fillMaxWidth()) {
		if (title != null) {
			Box(modifier = Modifier.padding(start = 12.dp)) {
				@OptIn(ExperimentalMaterial3ExpressiveApi::class)
				CompositionLocalProvider(
					LocalTextStyle provides MaterialTheme.typography.titleSmallEmphasized.copy(
						fontFamily = defaultFont(grade = 100, round = 100f)
					),
					LocalContentColor provides MaterialTheme.colorScheme.primary,
				) {
					title()
				}
			}
		}

		Column(
			modifier = Modifier.padding(top = if (title != null) 6.dp else 0.dp),
			verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
		) {
			content()
		}
	}
}

object SettingsGroupDefaults {
	val GapBetweenGroups = 24.dp
}
