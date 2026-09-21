package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalNavStack
import paige.navic.domain.models.settings.NavbarConfig
import paige.navic.domain.models.settings.NavbarTab
import paige.navic.ui.navigation.NavigationTab
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.viewmodels.NavtabsViewModel
import paige.navic.ui.viewmodel.RootViewModel

@Composable
fun BottomBar(
	modifier: Modifier = Modifier,
	containerColor: Color = NavigationBarDefaults.containerColor,
	windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
	enabled: Boolean = true
) {
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

	NavigationBar(
		modifier = modifier,
		containerColor = containerColor,
		windowInsets = windowInsets
	) {
		tabs.forEach { tab ->
			val selected = backStack.lastOrNull() == tab.destination
			NavigationBarItem(
				selected = selected,
				enabled = enabled,
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
				label = { Text(stringResource(tab.label)) }
			)
		}
	}
}
