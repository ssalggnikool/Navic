package paige.navic.ui.components.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_not_playing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalNavStack
import paige.navic.domain.models.settings.NavbarConfig
import paige.navic.domain.models.settings.NavbarTab
import paige.navic.icons.Icons
import paige.navic.icons.filled.Note
import paige.navic.icons.outlined.Menu
import paige.navic.icons.outlined.MenuOpen
import paige.navic.icons.outlined.Radio
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.WideNavigationRailItem
import paige.navic.ui.navigation.NavigationTab
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.viewmodels.NavtabsViewModel
import paige.navic.ui.viewmodel.RootViewModel

@Composable
fun SideBar() {
	val viewModel = koinViewModel<NavtabsViewModel>()
	val rootViewModel = koinViewModel<RootViewModel>()
	val backStack = LocalNavStack.current
	val state by viewModel.state.collectAsStateWithLifecycle()
	val tabs = (state.data ?: NavbarConfig.default).tabs.filter { tab -> tab.visible }.map { tab ->
		when (tab.id) {
			NavbarTab.Id.LIBRARY -> NavigationTab.LIBRARY
			NavbarTab.Id.ALBUMS -> NavigationTab.ALBUMS
			NavbarTab.Id.PLAYLISTS -> NavigationTab.PLAYLISTS
			NavbarTab.Id.ARTISTS -> NavigationTab.ARTISTS
			NavbarTab.Id.SEARCH -> NavigationTab.SEARCH
			NavbarTab.Id.GENRES -> NavigationTab.GENRES
			NavbarTab.Id.SONGS -> NavigationTab.SONGS
			NavbarTab.Id.RADIOS -> NavigationTab.RADIOS
			NavbarTab.Id.STATISTICS -> NavigationTab.STATISTICS
		}
	}

	val onTabSelected = { destination: Screen ->
		if (backStack.lastOrNull() == destination) {
			rootViewModel.requestScrollToTop()
		} else {
			backStack.apply {
				clear()
				add(destination)
			}
		}
	}

	val railState = rememberWideNavigationRailState()

	WideNavigationRail(
		state = railState,
		header = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				SideBarHeader(state = railState)
				SideBarMiniPlayer(railExpanded = railState.targetValue == WideNavigationRailValue.Expanded)
			}
		}
	) {
		tabs.forEach { tab ->
			val selected = backStack.lastOrNull() == tab.destination
			WideNavigationRailItem(
				selected = selected,
				onClick = dropUnlessResumed {
					onTabSelected(tab.destination)
				},
				icon = {
					if (selected) {
						Icon(tab.iconFilled, null)
					} else {
						Icon(tab.iconOutlined, null)
					}
				},
				label = { Text(stringResource(tab.label)) },
				railExpanded = railState.targetValue == WideNavigationRailValue.Expanded
			)
		}
	}
}

@Composable
private fun SideBarMiniPlayer(
	railExpanded: Boolean
) {
	val backStack = LocalNavStack.current

	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val song = playerState.currentSong
	val isRadio = song?.id?.startsWith("radio_") == true

	val padding by animateDpAsState(
		if (railExpanded) 8.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
	)
	val containerRadius by animateDpAsState(
		if (railExpanded) 18.dp else 12.dp,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
	)

	Surface(
		modifier = Modifier.padding(start = 24.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		shape = ContinuousRoundedRectangle(containerRadius),
		onClick = dropUnlessResumed {
			backStack.add(Screen.NowPlaying)
		}
	) {
		Row(
			modifier = Modifier.padding(padding.coerceAtLeast(0.dp)),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(contentAlignment = Alignment.Center) {
				CoverArt(
					modifier = Modifier.size(48.dp),
					coverArtId = song?.coverArtId,
					contentDescription = song?.title,
					shape = ContinuousRoundedRectangle(12.dp)
				)
				if (song?.coverArtId.isNullOrEmpty()) {
					Icon(
						imageVector = if (isRadio) Icons.Outlined.Radio else Icons.Filled.Note,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
					)
				}
			}
			AnimatedVisibility(
				visible = railExpanded,
				enter = fadeIn() + expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
				exit = fadeOut() + shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
			) {
				Column(Modifier.padding(start = 12.dp)) {
					Text(
						text = song?.title ?: stringResource(Res.string.info_not_playing),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.bodyMedium
					)
					song?.artistName?.let { artistName ->
						Text(
							text = artistName,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}
			}
		}
	}
}

@Composable
private fun SideBarHeader(
	state: WideNavigationRailState
) {
	val scope = rememberCoroutineScope()

	IconButton(
		modifier = Modifier.padding(start = 24.dp),
		onClick = {
			scope.launch {
				if (state.targetValue == WideNavigationRailValue.Expanded)
					state.collapse()
				else state.expand()
			}
		},
	) {
		if (state.targetValue == WideNavigationRailValue.Expanded) {
			Icon(Icons.Outlined.MenuOpen, null)
		} else {
			Icon(Icons.Outlined.Menu, null)
		}
	}
}
