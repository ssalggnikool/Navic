/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.FontOption
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Check
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.theme.googleSans

@Composable
fun FontsScreen() {
	val preferenceManager = koinInject<PreferenceManager>()
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.title_choose_font)) }) }
	) { contentPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			LazyColumn(
				modifier = Modifier.selectableGroup(),
				verticalArrangement = Arrangement.spacedBy(3.dp),
				contentPadding = contentPadding + PaddingValues(
					top = 16.dp, end = 16.dp, start = 16.dp
				)
			) {
				inbuiltFonts(
					onSelectFont = { preferenceManager.font = it },
					selectedFont = preferenceManager.font
				)
				externalFonts()
			}
		}
	}
}

private fun LazyListScope.heading(resource: Int) {
	item {
		Text(
			stringResource(resource),
			style = MaterialTheme.typography.titleSmallEmphasized,
			modifier = Modifier.padding(horizontal = 12.dp).semantics {
				heading()
			}
		)
	}
}

private fun LazyListScope.inbuiltFonts(
	onSelectFont: (FontOption) -> Unit,
	selectedFont: FontOption
) {
	heading(R.string.title_fonts_inbuilt)
	item {
		FontRow(
			fontName = "System",
			fontFamily = FontFamily.Default,
			index = 0,
			count = 2,
			onClick = { onSelectFont(FontOption.System) },
			selected = selectedFont == FontOption.System
		)
	}
	item {
		FontRow(
			fontName = "Google Sans",
			fontFamily = googleSans(),
			index = 1,
			count = 2,
			onClick = { onSelectFont(FontOption.GoogleSans) },
			selected = selectedFont == FontOption.GoogleSans
		)
		Spacer(Modifier.height(10.dp))
	}
}

private fun LazyListScope.externalFonts() {
	//heading(R.string.title_fonts_external)
}

@Composable
private fun FontRow(
	fontName: String,
	fontFamily: FontFamily?,
	selected: Boolean,
	index: Int,
	@Suppress("SameParameterValue")
	count: Int,
	onClick: () -> Unit
) {
	SegmentedListItem(
		onClick = onClick,
		selected = selected,
		colors = SegmentedListItemDefaults.segmentedColors(
			selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
			selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
			selectedSupportingContentColor = MaterialTheme.colorScheme.onSecondaryContainer
		),
		shapes = SegmentedListItemDefaults.segmentedShapes(
			index = index,
			count = count
		),
		content = {
			Text(fontName)
		},
		supportingContent = {
			Text(
				"The quick brown fox jumps over the lazy dog",
				fontFamily = fontFamily,
				modifier = Modifier.semantics { hideFromAccessibility() }
			)
		},
		trailingContent = {
			if (selected) {
				Box(Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) {
					Icon(
						Icons.Outlined.Check,
						contentDescription = null,
						modifier = Modifier.size(16.dp),
						tint = MaterialTheme.colorScheme.onPrimary
					)
				}
			}
		}
	)
}
