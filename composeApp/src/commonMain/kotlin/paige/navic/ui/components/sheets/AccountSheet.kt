package paige.navic.ui.components.sheets

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousCapsule
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.action_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer_enabled
import navic.composeapp.generated.resources.action_sleep_timer_queue_enabled
import navic.composeapp.generated.resources.action_sleep_timer_songs_enabled
import navic.composeapp.generated.resources.action_view_shares
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.LoginManager
import paige.navic.domain.manager.SleepTimerManager
import paige.navic.domain.manager.SleepTimerMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Logout
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.Monogram
import paige.navic.ui.components.common.SegmentedListItem
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.navigation.Screen
import paige.navic.ui.theme.positive
import paige.navic.ui.util.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountSheet(
	onDismissRequest: () -> Unit
) {
	val backStack = LocalNavStack.current
	val loginManager = koinInject<LoginManager>()
	val settings = koinInject<Settings>()

	var sleepTimerSheetOpen by rememberSaveable { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerMode by sleepTimerManager.mode.collectAsStateWithLifecycle()

	val sheetState = rememberBottomSheetState(
		initialValue = SheetValue.Hidden,
		enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
	)

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
				shape = ContinuousCapsule,
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
				shape = MaterialTheme.shapes.large
			) {
				Row(
					modifier = Modifier.padding(12.dp).fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Monogram(text = settings.getString("username", ""))

					Column(
						modifier = Modifier.weight(1f)
					) {
						Text(
							text = settings.getString("username", ""),
							style = MaterialTheme.typography.headlineSmall
						)

						Text(
							text = settings.getString("instanceUrl", ""),
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			Spacer(Modifier.height(9.dp))

			SegmentedListItem(
				shapes = SegmentedListItemDefaults.segmentedShapes(
					index = 0,
					count = 3
				),
				onClick = {
					animateToDismiss()
					backStack.add(Screen.ShareList)
				},
				leadingContent = { Icon(Icons.Outlined.Share, null) },
				content = { Text(stringResource(Res.string.action_view_shares)) }
			)

			SegmentedListItem(
				shapes = SegmentedListItemDefaults.segmentedShapes(
					index = 1,
					count = 3
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
							Res.string.action_sleep_timer_enabled,
							sleepTimerManager.timeLeft?.label() ?: ""
						)

						is SleepTimerMode.Songs -> stringResource(
							Res.string.action_sleep_timer_songs_enabled,
							mode.remaining
						)

						is SleepTimerMode.EndOfQueue -> stringResource(
							Res.string.action_sleep_timer_queue_enabled
						)

						else -> stringResource(Res.string.action_sleep_timer)
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
				leadingContent = { Icon(Icons.Outlined.Logout, null) },
				content = { Text(stringResource(Res.string.action_log_out)) }
			)
		}
	}

	if (sleepTimerSheetOpen) {
		SleepTimerSheet(onDismissRequest = { sleepTimerSheetOpen = false })
	}
}
