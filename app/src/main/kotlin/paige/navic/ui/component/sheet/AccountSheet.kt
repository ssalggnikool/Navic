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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.playback.SleepTimerManager
import paige.navic.domain.manager.playback.SleepTimerMode
import paige.navic.domain.manager.session.account.Account
import paige.navic.domain.manager.session.LoginManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.manager.session.canShare
import paige.navic.domain.manager.session.getUser
import paige.navic.domain.manager.session.supportsExtension
import paige.navic.domain.server.ServerExtension
import paige.navic.ui.component.common.Monogram
import paige.navic.ui.component.dialog.AlertDialog
import paige.navic.ui.component.common.SegmentedListItem
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.icons.Icons
import paige.navic.icons.filled.Settings
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Edit
import paige.navic.icons.outlined.Forum
import paige.navic.icons.outlined.Refresh
import paige.navic.icons.outlined.Share
import paige.navic.ui.navigation.Screen
import paige.navic.designsystem.theme.positive
import paige.navic.ui.component.common.ServerTypeIcon
import paige.navic.ui.util.label

@Composable
private fun AccountAvatar(
	modifier: Modifier = Modifier,
	url: String?,
	name: String,
	cacheKey: String,
	size: Dp,
	imageLoader: ImageLoader,
	coilPlatformContext: coil3.PlatformContext
) {
	if (url == null) {
		Monogram(modifier = modifier.size(size), text = name)
		return
	}

	val model = remember(url, cacheKey) {
		ImageRequest.Builder(coilPlatformContext)
			.data(url)
			.memoryCacheKey(cacheKey)
			.diskCacheKey(cacheKey)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.diskCachePolicy(CachePolicy.ENABLED)
			.build()
	}

	AsyncImage(
		model = model,
		imageLoader = imageLoader,
		contentDescription = null,
		modifier = modifier
			.size(size)
			.clip(CircleShape)
	)
}

@Composable
private fun AccountRow(
	account: Account,
	active: Boolean,
	avatarUrl: String?,
	imageLoader: ImageLoader,
	coilPlatformContext: coil3.PlatformContext,
	index: Int = 0,
	count: Int = 1,
	onClick: () -> Unit,
	onRemove: (() -> Unit)? = null
) {
	val accountName = account.username?.ifBlank { account.baseUrl } ?: account.baseUrl
	val avatarSize = if (active) 56.dp else 40.dp

	val leadingContent: @Composable () -> Unit = {
		AccountAvatar(
			url = avatarUrl,
			name = accountName,
			cacheKey = "avatar:${account.id}:${account.username}",
			size = avatarSize,
			imageLoader = imageLoader,
			coilPlatformContext = coilPlatformContext
		)
	}

	if (active) {
		Surface(
			color = MaterialTheme.colorScheme.surfaceContainer,
			shape = MaterialTheme.shapes.large,
			onClick = onClick
		) {
			Row(
				modifier = Modifier.padding(12.dp).fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				leadingContent()
				Column(modifier = Modifier.weight(1f)) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(accountName)
						account.serverType?.let {
							ServerTypeIcon(serverType = it, modifier = Modifier.size(16.dp))
						}
					}
					Text(
						text = account.baseUrl,
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
	} else {
		SegmentedListItem(
			onClick = onClick,
			shapes = SegmentedListItemDefaults.segmentedShapes(index = index, count = count),
			leadingContent = leadingContent,
			trailingContent = {
				IconButton(onClick = { onRemove?.invoke() }) {
					Icon(
						imageVector = Icons.Outlined.Delete,
						contentDescription = stringResource(R.string.action_remove_account)
					)
				}
			},
			supportingContent = {
				Text(
					text = account.baseUrl,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			},
			content = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(accountName)
					account.serverType?.let {
						ServerTypeIcon(serverType = it, modifier = Modifier.size(16.dp))
					}
				}
			}
		)
	}
}

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
	val accounts by sessionManager.accounts.collectAsStateWithLifecycle()
	var accountToRemove by remember { mutableStateOf<Account?>(null) }

	var sleepTimerSheetOpen by rememberSaveable { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerMode by sleepTimerManager.mode.collectAsStateWithLifecycle()

	val sheetState = rememberBottomSheetState(
		initialValue = SheetValue.Hidden,
		enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
	)
	var profilePictureUrl by remember { mutableStateOf<String?>(null) }
	val coilPlatformContext = LocalCoilPlatformContext.current
	val imageLoader = koinInject<ImageLoader>()

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
			accounts.firstOrNull { it.id == sessionManager.activeAccountId }?.let { activeAccount ->
				AccountRow(
					account = activeAccount,
					active = true,
					avatarUrl = profilePictureUrl,
					imageLoader = imageLoader,
					coilPlatformContext = coilPlatformContext,
					onClick = {
						animateToDismiss()
						backStack.add(Screen.Settings.ConnectionOptions)
					}
				)
			}

			Spacer(Modifier.height(8.dp))

			Column(
				verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
			) {
				val alternateAccounts = accounts.filterNot { it.id == sessionManager.activeAccountId }

				alternateAccounts.forEachIndexed { index, account ->
					AccountRow(
						account = account,
						active = false,
						index = index,
						count = accounts.size + 1,
						avatarUrl = remember(account.id) {
							runCatching { sessionManager.getAvatarUrl(account) }.getOrNull()
						},
						imageLoader = imageLoader,
						coilPlatformContext = coilPlatformContext,
						onClick = { loginManager.switchAccount(account.id) },
						onRemove = { accountToRemove = account }
					)
				}

				SegmentedListItem(
					onClick = {
						animateToDismiss()
						backStack.add(Screen.Login)
					},
					shapes = SegmentedListItemDefaults.segmentedShapes(
						index = alternateAccounts.size,
						count = alternateAccounts.size + 1
					),
					leadingContent = { Icon(Icons.Outlined.Add, null) },
					content = { Text(stringResource(R.string.action_add_account)) }
				)
			}

			Spacer(Modifier.height(4.dp))

			Column(
				verticalArrangement = Arrangement.spacedBy(SegmentedListItemDefaults.SegmentedGap)
			) {
				val isSharingAllowed = sessionManager.getUser()?.canShare() == true
				val supportsChat = sessionManager.supportsExtension(ServerExtension.SUBSONIC_CHAT)

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
						accounts.firstOrNull { it.id == sessionManager.activeAccountId }
							?.let { accountToRemove = it }
					},
					colors = SegmentedListItemDefaults.segmentedErrorColors(),
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Delete,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.error
						)
					},
					content = { Text(stringResource(R.string.action_remove_account)) }
				)
			}
		}
	}

	if (sleepTimerSheetOpen) {
		SleepTimerSheet(onDismissRequest = { sleepTimerSheetOpen = false })
	}

	accountToRemove?.let { account ->
		AlertDialog(
			onDismissRequest = { accountToRemove = null },
			title = { Text(stringResource(R.string.title_remove_account)) },
			text = {
				Text(stringResource(R.string.notice_remove_server, account.baseUrl))
			},
			confirmButton = {
				TextButton(
					onClick = {
						loginManager.removeAccount(account.id)
						accountToRemove = null
					}
				) {
					Text(stringResource(R.string.action_remove_account))
				}
			},
			dismissButton = {
				TextButton(onClick = { accountToRemove = null }) {
					Text(stringResource(R.string.action_cancel))
				}
			}
		)
	}
}
