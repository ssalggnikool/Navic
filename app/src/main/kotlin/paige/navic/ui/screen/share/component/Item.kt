/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.share.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainShare
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.component.common.SwipeToDismissBox
import paige.navic.util.toHoursMinutesSeconds
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun ShareListScreenItem(
	modifier: Modifier = Modifier,
	share: DomainShare,
	onClick: () -> Unit,
	onSwipeToDelete: () -> Unit
) {
	var currentTime by remember { mutableStateOf(Clock.System.now()) }
	val scope = rememberCoroutineScope()
	val dismissState = rememberSwipeToDismissBoxState()
	val preferenceManager = koinInject<PreferenceManager>()

	LaunchedEffect(share.expiresAt) {
		while (true) {
			delay(1.seconds)
			currentTime = Clock.System.now()
		}
	}

	SwipeToDismissBox(
		state = dismissState,
		onDismiss = {
			if (it != SwipeToDismissBoxValue.Settled) onSwipeToDelete()
			scope.launch { dismissState.reset() }
		},
		backgroundContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(MaterialTheme.shapes.small)
					.background(MaterialTheme.colorScheme.errorContainer)
					.padding(horizontal = 20.dp)
			) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.align(
						when (dismissState.dismissDirection) {
							SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
							else -> Alignment.CenterEnd
						}
					)
				)
			}
		}
	) {
		ListItem(
			modifier = modifier,
			leadingContent = {
				CoverArt(
					coverArtId = share.items.firstOrNull()?.coverArtId,
					modifier = Modifier.size(60.dp),
					shape = preferenceManager.coverArtShape.decreasedShape
				)
			},
			content = { share.description?.let { Text(it) } },
			supportingContent = { Text(stringResource(R.string.info_shared_by, share.username)) },
			overlineContent = share.expiresAt?.let { expires ->
				{
					val remaining = expires - currentTime
					if (remaining.isPositive()) {
						Text(
							stringResource(
								R.string.info_share_expires_in,
								remaining.toHoursMinutesSeconds()
							)
						)
					} else {
						Text(stringResource(R.string.info_share_expired))
					}
				}
			},
			onClick = onClick,
			onLongClick = onClick
		)
	}
}
