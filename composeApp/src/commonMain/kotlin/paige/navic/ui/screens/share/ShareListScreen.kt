package paige.navic.ui.screens.share

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_no_shares
import navic.composeapp.generated.resources.title_shares
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.di.LocalBottomBarScrollManager
import paige.navic.di.LocalSnackBarState
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.ShareManager
import paige.navic.domain.models.settings.BottomBarVisibilityMode
import paige.navic.icons.Icons
import paige.navic.icons.filled.ShareOff
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.components.sheets.ShareSheet
import paige.navic.ui.core.UiState
import paige.navic.ui.screens.share.components.ShareListScreenItem
import paige.navic.ui.screens.share.viewmodels.ShareListViewModel
import paige.navic.ui.util.withoutTop

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareListScreen() {
	val viewModel = koinViewModel<ShareListViewModel>()

	val sharesState by viewModel.sharesState.collectAsStateWithLifecycle()
	val selectedShare by viewModel.selectedShare.collectAsStateWithLifecycle()
	val isRefreshingFlow by viewModel.isRefreshing.collectAsStateWithLifecycle()

	val shareManager = koinInject<ShareManager>()
	val preferenceManager = koinInject<PreferenceManager>()
	val snackBarState = LocalSnackBarState.current
	val scope = rememberCoroutineScope()

	var deletionId by remember { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_shares)) }) },
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (preferenceManager.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = contentPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = sharesState !is UiState.Loading && !isRefreshingFlow,
			onRefresh = { viewModel.refreshShares() },
			key = listOf(sharesState, isRefreshingFlow)
		) {
			Crossfade(sharesState) { stateValue ->
				LazyVerticalGrid(
					modifier = Modifier.fillMaxSize(),
					columns = GridCells.Fixed(1),
					contentPadding = contentPadding.withoutTop(),
					state = viewModel.gridState,
					verticalArrangement = if ((stateValue as? UiState.Success)?.data?.isEmpty() == true)
						Arrangement.Center
					else Arrangement.Top
				) {
					when (stateValue) {
						is UiState.Loading -> {
							return@LazyVerticalGrid
						}

						is UiState.Error -> artGridError(stateValue)
						is UiState.Success -> {
							items(stateValue.data, { it.id }) { share ->
								ShareListScreenItem(
									modifier = Modifier.animateItem(fadeInSpec = null),
									share = share,
									onClick = { viewModel.updateSelection(share) },
									onSwipeToDelete = { deletionId = share.id }
								)
							}
							if (stateValue.data.isEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									ContentUnavailable(
										icon = Icons.Filled.ShareOff,
										label = stringResource(Res.string.info_no_shares)
									)
								}
							}
						}
					}
				}
			}
		}
	}

	selectedShare?.let { selectedShare ->
		ShareSheet(
			onDismissRequest = viewModel::clearSelection,
			onShare = {
				scope.launch {
					try {
						shareManager.shareString(selectedShare.url)
					} catch (ex: Exception) {
						val message = ex.message ?: getString(Res.string.info_error)
						snackBarState.showSnackbar(message)
					}
				}
			},
			onDelete = { deletionId = selectedShare.id }
		)
	}

	DeletionDialog(
		endpoint = DeletionEndpoint.SHARE,
		id = deletionId,
		onIdClear = { deletionId = null },
		onRefresh = viewModel::refreshShares
	)
}
