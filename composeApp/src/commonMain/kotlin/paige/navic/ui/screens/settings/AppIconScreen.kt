package paige.navic.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_app_icon
import navic.composeapp.generated.resources.option_choose_app_icon
import navic.composeapp.generated.resources.subtitle_app_icon_designer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.AppIconManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.AppIconVariant
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Info
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsAppIconScreen() {
	val appIconManager = koinInject<AppIconManager>()
	val preferenceManager = koinInject<PreferenceManager>()
	// in case user tries to change the icon multiple times
	var changed by rememberSaveable { mutableStateOf(false) }
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.option_choose_app_icon)) }) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp),
				verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
			) {
				SettingsGroup(
					modifier = Modifier.selectableGroup()
				) {
					AppIconVariant.entries.forEachIndexed { index, variant ->
						val selected = preferenceManager.appIconVariant == variant
						SegmentedListItem(
							selected = selected,
							onClick = {
								if (!changed && !selected) {
									changed = true
									appIconManager.setVariant(variant)
								}
							},
							content = { Text(variant.name) },
							supportingContent = {
								Text(
									text = stringResource(
										Res.string.subtitle_app_icon_designer,
										variant.designer
									)
								)
							},
							leadingContent = {
								RadioButton(
									selected = selected,
									onClick = null
								)
							},
							trailingContent = { AppIconItemPreview(variant) },
							shapes = SegmentedListItemDefaults.segmentedShapes(
								index = index,
								count = AppIconVariant.entries.count()
							)
						)
					}
				}

				Row(
					modifier = Modifier.padding(horizontal = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(16.dp)
				) {
					Icon(
						Icons.Outlined.Info,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						stringResource(Res.string.info_app_icon),
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@Composable
fun AppIconItemPreview(variant: AppIconVariant, modifier: Modifier = Modifier) {
	val appIconManager = koinInject<AppIconManager>()
	val icon = remember(variant) { appIconManager.getIcon(variant) }

	val iconModifier = modifier
		.size(48.dp)
		.clip(MaterialTheme.shapes.medium)

	when (icon) {
		is ImageBitmap -> Image(
			bitmap = icon,
			contentDescription = null,
			modifier = iconModifier
		)

		is Painter -> Image(
			painter = icon,
			contentDescription = null,
			modifier = iconModifier
		)
	}
}
