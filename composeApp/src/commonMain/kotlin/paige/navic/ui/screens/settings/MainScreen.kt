package paige.navic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.subtitle_about
import navic.composeapp.generated.resources.subtitle_appearance
import navic.composeapp.generated.resources.subtitle_bottom_app_bar
import navic.composeapp.generated.resources.subtitle_data_storage
import navic.composeapp.generated.resources.subtitle_developer
import navic.composeapp.generated.resources.subtitle_now_playing
import navic.composeapp.generated.resources.subtitle_playback
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_bottom_app_bar
import navic.composeapp.generated.resources.title_data_storage
import navic.composeapp.generated.resources.title_developer
import navic.composeapp.generated.resources.title_now_playing
import navic.composeapp.generated.resources.title_playback
import navic.composeapp.generated.resources.title_settings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.PreferenceManager
import paige.navic.icons.Icons
import paige.navic.icons.filled.BottomNavigation
import paige.navic.icons.filled.Info
import paige.navic.icons.filled.Palette
import paige.navic.icons.filled.Play
import paige.navic.icons.outlined.ChevronForward
import paige.navic.icons.outlined.Code
import paige.navic.icons.outlined.DataTable
import paige.navic.icons.outlined.Note
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.theme.defaultFont

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen() {
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_settings)) }) }
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
		) {
			SettingsGroup {
				PageRow(
					destination = Screen.Settings.Appearance,
					icon = Icons.Filled.Palette,
					iconSize = 24.dp,
					title = Res.string.title_appearance,
					subtitle = Res.string.subtitle_appearance,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 6)
				)
				PageRow(
					destination = Screen.Settings.NowPlaying,
					icon = Icons.Filled.Play,
					iconSize = 24.dp,
					title = Res.string.title_now_playing,
					subtitle = Res.string.subtitle_now_playing,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 1, count = 6)
				)
				PageRow(
					destination = Screen.Settings.BottomAppBar,
					icon = Icons.Filled.BottomNavigation,
					iconSize = 24.dp,
					title = Res.string.title_bottom_app_bar,
					subtitle = Res.string.subtitle_bottom_app_bar,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 2, count = 6)
				)
				PageRow(
					destination = Screen.Settings.Playback,
					icon = Icons.Outlined.Note,
					iconSize = 24.dp,
					title = Res.string.title_playback,
					subtitle = Res.string.subtitle_playback,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 3, count = 6)
				)
				PageRow(
					destination = Screen.Settings.DataStorage,
					icon = Icons.Outlined.DataTable,
					iconSize = 24.dp,
					title = Res.string.title_data_storage,
					subtitle = Res.string.subtitle_data_storage,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 4, count = 6)
				)
				PageRow(
					destination = Screen.Settings.Developer,
					icon = Icons.Outlined.Code,
					iconSize = 24.dp,
					title = Res.string.title_developer,
					subtitle = Res.string.subtitle_developer,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 5, count = 6)
				)
			}
			SettingsGroup {
				PageRow(
					destination = Screen.Settings.About,
					icon = Icons.Filled.Info,
					title = Res.string.title_about,
					subtitle = Res.string.subtitle_about,
					shapes = SegmentedListItemDefaults.segmentedShapes(index = 0, count = 1)
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageRow(
	destination: Screen? = null,
	icon: ImageVector,
	iconSize: Dp = 22.dp,
	title: StringResource,
	subtitle: StringResource,
	shapes: ListItemShapes
) {
	val backStack = LocalNavStack.current
	val preferenceManager = koinInject<PreferenceManager>()
	SegmentedListItem(
		shapes = shapes,
		onClick = dropUnlessResumed {
			destination?.let { destination ->
				backStack.lastOrNull()?.let {
					if (it is Screen.Settings) {
						if (it !is Screen.Settings.Root) {
							backStack.removeLastOrNull()
						}
						backStack.add(destination)
					}
				}
			}
		},
		contentPadding = PaddingValues(if (preferenceManager.theme.isMaterialLike()) 16.dp else 12.dp)
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			if (preferenceManager.theme.isMaterialLike()) {
				Column(
					modifier = Modifier
						.size(40.dp)
						.background(MaterialTheme.colorScheme.primary, CircleShape),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Icon(
						imageVector = icon,
						contentDescription = null,
						modifier = Modifier.size(iconSize),
						tint = MaterialTheme.colorScheme.onPrimary
					)
				}
			} else {
				Icon(
					icon,
					contentDescription = null,
					modifier = Modifier.padding(start = 8.dp, end = 5.dp).size(22.dp),
					tint = MaterialTheme.colorScheme.primary
				)
			}
			Column(
				Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(1.dp)
			) {
				Text(
					stringResource(title),
					style = MaterialTheme.typography.titleSmall.copy(
						fontFamily = defaultFont(100),
						fontSize = 16.sp,
						lineHeight = 16.sp
					)
				)
				Text(
					stringResource(subtitle),
					style = MaterialTheme.typography.bodyMedium.copy(
						fontFamily = defaultFont(grade = 10),
						lineHeight = 14.sp
					),
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			if (!preferenceManager.theme.isMaterialLike()) {
				Icon(
					Icons.Outlined.ChevronForward,
					null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
