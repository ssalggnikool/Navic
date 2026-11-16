/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.search.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.ui.screen.search.SearchCategory

@Composable
fun SearchScreenChips(
	selectedCategory: SearchCategory,
	onCategorySelect: (SearchCategory) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.selectableGroup(),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		SearchCategory.entries.forEach { category ->
			val isSelected = category == selectedCategory
			FilterChip(
				modifier = Modifier
					.animateContentSize(
						if (isSelected)
							MaterialTheme.motionScheme.fastSpatialSpec()
						else MaterialTheme.motionScheme.defaultEffectsSpec()
					),
				selected = isSelected,
				onClick = {
					onCategorySelect(category)
				},
				label = {
					Text(
						stringResource(category.res),
						maxLines = 1
					)
				},
				shape = MaterialTheme.shapes.small,
				leadingIcon = if (isSelected) {
					{
						Icon(
							imageVector = Icons.Outlined.Check,
							contentDescription = null,
							modifier = Modifier.size(FilterChipDefaults.IconSize)
						)
					}
				} else {
					null
				}
			)
		}
	}
}
