/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.nowPlaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSheetState
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.settings.NowPlayingBackgroundStyle
import paige.navic.domain.model.settings.ToolbarPosition
import paige.navic.icons.Icons
import paige.navic.icons.outlined.KeyboardArrowDown
import paige.navic.icons.outlined.List
import paige.navic.icons.outlined.Lyrics
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.component.common.BlendBackground
import paige.navic.ui.component.layout.SheetScaffold
import paige.navic.ui.component.layout.TopBarButton
import paige.navic.ui.component.toolbar.SheetActionButton
import paige.navic.ui.component.toolbar.SheetToolbar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.nowPlaying.component.controls.NowPlayingArtworkPager
import paige.navic.ui.screen.nowPlaying.component.rows.NowPlayingControlsRow
import paige.navic.ui.screen.nowPlaying.viewmodel.NowPlayingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen() {
	val preferenceManager = koinInject<PreferenceManager>()
	val player = koinInject<MediaPlayerViewModel>()
	val backStack = LocalNavStack.current

	val currentScreen = backStack.lastOrNull()
	val isPlayerCurrent = currentScreen is Screen.NowPlaying
		|| currentScreen is Screen.Queue
		|| currentScreen is Screen.PlaybackSpeed
		|| currentScreen is Screen.SongDetailSheet

	val playerState by player.uiState.collectAsStateWithLifecycle()
	val song = playerState.currentSong

	val viewModel = koinViewModel<NowPlayingViewModel> { parametersOf(player) }
	val songIsStarred by viewModel.songIsStarred.collectAsStateWithLifecycle()
	val songRating by viewModel.songRating.collectAsStateWithLifecycle()

	val sheetState = LocalSheetState.current
	val closeScope = rememberCoroutineScope()
	val animateToDismiss = dropUnlessResumed {
		closeScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			if (!sheetState.isVisible) {
				backStack.remove(Screen.NowPlaying)
			}
		}
	}

	SheetScaffold(
		toolbar = { windowInsets ->
			SheetToolbar(
				modifier = Modifier.alpha(if (isPlayerCurrent) 1f else 0f),
				windowInsets = windowInsets,
				title = {
					Text(stringResource(R.string.title_now_playing))
				},
				navigationIcon = {
					TopBarButton(
						onClick = animateToDismiss,
						content = {
							Icon(
								imageVector = Icons.Outlined.KeyboardArrowDown,
								contentDescription = stringResource(R.string.action_navigate_back)
							)
						}
					)
				},
				actions = {
					SheetActionButton(
						icon = Icons.Outlined.Lyrics,
						contentDescription = stringResource(R.string.action_lyrics),
						onClick = dropUnlessResumed { backStack.add(Screen.Lyrics) },
						isStartRounded = true
					)
					SheetActionButton(
						icon = Icons.Outlined.List,
						contentDescription = stringResource(R.string.action_queue),
						onClick = dropUnlessResumed { backStack.add(Screen.Queue) },
						isEndRounded = true
					)
				}
			)
		}
	) { contentPadding ->
		Box(Modifier.fillMaxSize()) {
			if (preferenceManager.nowPlayingBackgroundStyle
				== NowPlayingBackgroundStyle.Dynamic
			) {
				BlendBackground(
					coverArtId = song?.coverArtId,
					isPaused = playerState.isPaused
				)
			}
			if (!isPlayerCurrent) return@Box
			BoxWithConstraints(
				modifier = Modifier
					.padding(horizontal = 8.dp)
					.fillMaxSize()
			) {
				val isLandscape = maxWidth > maxHeight
				val toolbarPosition = preferenceManager.nowPlayingToolbarPosition
				val padding = when {
					isLandscape -> contentPadding
					toolbarPosition == ToolbarPosition.Top -> contentPadding.plus(
						PaddingValues(
							bottom = 40.dp
						)
					)

					toolbarPosition == ToolbarPosition.Bottom -> contentPadding.plus(
						PaddingValues(
							top = 40.dp
						)
					)

					else -> contentPadding
				}
				if (isLandscape) {
					Row(
						modifier = Modifier.fillMaxSize().padding(padding),
						horizontalArrangement = Arrangement.SpaceEvenly,
						verticalAlignment = Alignment.CenterVertically
					) {
						NowPlayingArtworkPager(
							modifier = Modifier.weight(1f).fillMaxHeight(),
							isLandscape = true
						)
						NowPlayingControlsRow(
							modifier = Modifier.weight(1f).fillMaxHeight(),
							isLandscape = true,
							songIsStarred = songIsStarred,
							onSetSongIsStarred = { viewModel.starSong(it) },
							songRating = songRating,
							onSetSongRating = { viewModel.rateSong(it) }
						)
					}
				} else {
					Column(
						modifier = Modifier.fillMaxSize().padding(padding),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					) {
						NowPlayingArtworkPager(
							modifier = Modifier.weight(1f).fillMaxWidth(),
							isLandscape = false
						)
						NowPlayingControlsRow(
							modifier = Modifier.weight(1f),
							isLandscape = false,
							songIsStarred = songIsStarred,
							onSetSongIsStarred = { viewModel.starSong(it) },
							songRating = songRating,
							onSetSongRating = { viewModel.rateSong(it) }
						)
					}
				}
			}
		}
	}
}
