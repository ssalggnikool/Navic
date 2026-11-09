/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.playback.SleepTimerManager
import paige.navic.domain.manager.playback.SleepTimerMode
import paige.navic.domain.manager.session.LoginManager
import paige.navic.domain.server.ServerExtension
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.session.canShare
import paige.navic.domain.manager.session.getUser
import paige.navic.domain.manager.session.supportsExtension
import paige.navic.ui.component.common.Monogram
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.filled.Settings
import paige.navic.ui.icons.outlined.Bedtime
import paige.navic.ui.icons.outlined.Edit
import paige.navic.ui.icons.outlined.Forum
import paige.navic.ui.icons.outlined.Logout
import paige.navic.ui.icons.outlined.Refresh
import paige.navic.ui.icons.outlined.Share
import paige.navic.ui.navigation.Screen
import paige.navic.ui.theme.positive
import paige.navic.ui.util.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
	onDismissRequest: () -> Unit
) {
	val backStack = LocalNavStack.current
	val loginManager = koinInject<LoginManager>()
	val sessionManager = koinInject<SessionManager>()
	val syncManager = koinInject<SyncManager>()
	val syncState by syncManager.syncState.collectAsStateWithLifecycle()

	var sleepTimerSheetOpen by rememberSaveable { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerMode by sleepTimerManager.mode.collectAsStateWithLifecycle()

	val sheetState = rememberBottomSheetState(
		initialValue = SheetValue.Hidden,
		enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
	)
	var profilePictureUrl by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(Unit) {
		try {
			sessionManager.username
				.takeIf { it.isNotBlank() }
				?.let { username ->
					profilePictureUrl = sessionManager.api.getAvatarUrl(username)
				}
		} catch (_: Exception) {
			profilePictureUrl = null
		}
	}

	val scope = rememberCoroutineScope()
	val animateToDismiss = {
		scope
			.launch { sheetState.hide() }
			.invokeOnCompletion {
				if (!sheetState.isVisible) {
					onDismissRequest()
				}
			}
	}

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 12.dp,
					right = 12.dp
				)
			)
		},
		dragHandle = {
			Surface(
				modifier = Modifier.padding(vertical = 6.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				shape = CircleShape,
			) {
				Box(Modifier.size(width = 32.dp, height = 4.dp))
			}
		}
	) {
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
		) {
			Surface(
				color = MaterialTheme.colorScheme.surfaceContainer,
				shape = MaterialTheme.shapes.large,
				onClick = {
					animateToDismiss()
					backStack.add(Screen.Settings.ConnectionOptions)
				}
			) {
				Row(
					modifier = Modifier
						.padding(12.dp)
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					if (profilePictureUrl != null) {
						SubcomposeAsyncImage(
							model = profilePictureUrl,
							contentDescription = null,
							modifier = Modifier
								.size(56.dp)
								.clip(CircleShape),
							loading = {
								Surface(color = MaterialTheme.colorScheme.secondary) {
									Box(modifier = Modifier.fillMaxSize())
								}
							},
							error = { Monogram(text = sessionManager.username) }
						)
					} else {
						Monogram(text = sessionManager.username)
					}
					Column(
						modifier = Modifier.weight(1f)
					) {
						Text(
							text = sessionManager.username,
							style = MaterialTheme.typography.headlineSmall
						)

						Text(
							text = sessionManager.instanceUrl,
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					Icon(
						imageVector = Icons.Outlined.Edit,
						contentDescription = stringResource(R.string.action_edit),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			Spacer(Modifier.height(8.dp))

			Column(
				verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
			) {
				val isSharingAllowed = sessionManager.getUser()?.canShare() == true
				val supportsChat = sessionManager.supportsExtension(ServerExtension.CHAT)

				var count = 1

				if (isSharingAllowed) count++
				if (supportsChat) count++

				if (isSharingAllowed) {
					SegmentedListItem(
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 0,
							count = count
						),
						onClick = {
							animateToDismiss()
							backStack.add(Screen.ShareList)
						},
						leadingContent = { Icon(Icons.Outlined.Share, null) },
						content = { Text(stringResource(R.string.action_view_shares)) }
					)
				}

				if (supportsChat) {
					SegmentedListItem(
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = count
						),
						onClick = {
							animateToDismiss()
							backStack.add(Screen.Chat)
						},
						leadingContent = { Icon(Icons.Outlined.Forum, null) },
						content = { Text(stringResource(R.string.title_chat)) }
					)
				}

				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = count - 1,
						count = count
					),
					onClick = { sleepTimerSheetOpen = true },
					leadingContent = {
						val tint = when (sleepTimerMode) {
							!is SleepTimerMode.Disabled -> MaterialTheme.colorScheme.positive
							else -> LocalContentColor.current
						}
						Icon(
							imageVector = Icons.Outlined.Bedtime,
							contentDescription = null,
							tint = tint
						)
					},
					content = {
						val label = when (val mode = sleepTimerMode) {
							is SleepTimerMode.Time -> stringResource(
								R.string.action_sleep_timer_enabled,
								sleepTimerManager.timeLeft?.label() ?: ""
							)

							is SleepTimerMode.Songs -> stringResource(
								R.string.action_sleep_timer_songs_enabled,
								mode.remaining
							)

							is SleepTimerMode.EndOfQueue -> stringResource(
								R.string.action_sleep_timer_queue_enabled
							)

							else -> stringResource(R.string.action_sleep_timer)
						}
						val color = when (sleepTimerMode) {
							!is SleepTimerMode.Disabled -> MaterialTheme.colorScheme.positive
							else -> LocalContentColor.current
						}
						Text(
							text = label,
							color = color
						)
					}
				)
			}

			Spacer(Modifier.height(4.dp))

			Column(
				verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
			) {
				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = 0,
						count = 3
					),
					onClick = {
						if (!syncState.isSyncing) {
							syncManager.triggerManualSync()
						} else {
							syncManager.stopPeriodicSync()
						}
					},
					leadingContent = {
						if (!syncState.isSyncing) {
							Icon(Icons.Outlined.Refresh, null)
						} else {
							CircularProgressIndicator(
								progress = { syncState.progress },
								modifier = Modifier.size(24.dp),
								color = MaterialTheme.colorScheme.secondary,
								trackColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						}
					},
					content = {
						val label = if (syncState.isSyncing) {
							stringResource(syncState.message)
						} else {
							stringResource(R.string.action_trigger_sync)
						}

						Text(label)
					}
				)

				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = 1,
						count = 3
					),
					onClick = {
						animateToDismiss()
						backStack.add(Screen.Settings.Root)
					},
					leadingContent = { Icon(Icons.Filled.Settings, null) },
					content = { Text(stringResource(R.string.title_settings)) }
				)

				SegmentedListItem(
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = 2,
						count = 3
					),
					onClick = {
						animateToDismiss()
						loginManager.logout()
						backStack.clear()
						backStack.add(Screen.Login)
					},
					colors = SegmentedListItemDefaults.segmentedErrorColors(),
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Logout,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.error
						)
					},
					content = { Text(stringResource(R.string.action_log_out)) }
				)
			}
		}
	}

	if (sleepTimerSheetOpen) {
		SleepTimerSheet(onDismissRequest = { sleepTimerSheetOpen = false })
	}
}
