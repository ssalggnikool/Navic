package paige.navic.ui.screens.login.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.action_open_settings
import navic.composeapp.generated.resources.info_login_description
import navic.composeapp.generated.resources.notice_local_network_denied
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.subtitle_local_network_denied
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.LoginManager
import paige.navic.domain.manager.PermissionManager
import paige.navic.icons.Icons
import paige.navic.domain.manager.NotificationManager
import paige.navic.icons.outlined.Error
import paige.navic.ui.components.common.SegmentedListButton
import paige.navic.ui.components.common.SegmentedListButtonDefaults
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.components.dialogs.NotificationPermissionDialog
import paige.navic.ui.core.LoginUiState
import paige.navic.ui.navigation.Screen
import paige.navic.ui.theme.defaultFont

@Composable
fun LoginScreenContent(innerPadding: PaddingValues) {
	val viewModel = koinInject<LoginManager>()
	val loginState by viewModel.loginState.collectAsStateWithLifecycle()

	val instanceState = viewModel.instanceState
	val usernameState = viewModel.usernameState
	val passwordState = viewModel.passwordState

	val isBusy = loginState is LoginUiState.Loading || loginState is LoginUiState.Syncing

	val haptics = LocalHapticFeedback.current
	val backStack = LocalNavStack.current
	val focusManager = LocalFocusManager.current

	val instanceFocusRequester = remember { FocusRequester() }
	val usernameFocusRequester = remember { FocusRequester() }
	val passwordFocusRequester = remember { FocusRequester() }

	val permissionManager = koinInject<PermissionManager>()
	val notificationManager = koinInject<NotificationManager>()
	val loginScope = rememberCoroutineScope()
	var localNetworkDenied by rememberSaveable { mutableStateOf(false) }
	var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }

	val performLoginAction: () -> Unit = {
		loginScope.launch {
			if (!viewModel.login()) {
				haptics.performHapticFeedback(HapticFeedbackType.Reject)
				when {
					viewModel.instanceError -> instanceFocusRequester.requestFocus()
					viewModel.usernameError -> usernameFocusRequester.requestFocus()
					viewModel.passwordError -> passwordFocusRequester.requestFocus()
				}
			}
		}
	}

	val login: () -> Unit = {
		loginScope.launch {
			withContext(Dispatchers.IO) {
				if (viewModel.isLocalNetworkInstance()) {
					if (!permissionManager.requestLocalNetworkPermission()) {
						localNetworkDenied = true
						return@withContext
					}
				}
				showNotificationPermissionDialog = true
			}
		}
	}

	LaunchedEffect(loginState) {
		if (loginState is LoginUiState.Success) {
			backStack.clear()
			backStack.add(Screen.Library())
		}
	}

	Box {
		LoginScreenProgress(
			modifier = Modifier
				.align(Alignment.TopCenter)
				.padding(top = innerPadding.calculateTopPadding()),
			isBusy = isBusy,
			loginUiState = loginState
		)

		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.consumeWindowInsets(innerPadding)
				.imePadding(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Column(
				modifier = Modifier
					.weight(1f)
					.widthIn(max = 600.dp)
					.verticalScroll(rememberScrollState())
			) {
				Spacer(Modifier.weight(1f))

				Text(
					text = stringResource(Res.string.action_log_in),
					style = MaterialTheme.typography.headlineMedium,
					fontFamily = defaultFont(round = 100f),
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				Text(
					text = stringResource(Res.string.info_login_description),
					modifier = Modifier.padding(horizontal = 16.dp)
				)

				Spacer(Modifier.height(8.dp))

				LoginScreenError(loginUiState = loginState)

				LoginScreenFields(
					isBusy = isBusy,
					instanceState = instanceState,
					instanceError = viewModel.instanceError,
					instanceFocusRequester = instanceFocusRequester,
					onInstanceFocusChanged = { viewModel.validateInstance() },
					usernameState = usernameState,
					usernameError = viewModel.usernameError,
					usernameFocusRequester = usernameFocusRequester,
					onUsernameFocusChanged = { viewModel.validateUsername() },
					passwordState = passwordState,
					passwordError = viewModel.passwordError,
					passwordFocusRequester = passwordFocusRequester,
					onPasswordFocusChanged = { viewModel.validatePassword() },
					onLogin = login
				)

				Spacer(Modifier.height(12.dp))

				Text(
					text = stringResource(Res.string.option_custom_headers),
					color = MaterialTheme.colorScheme.primary,
					textDecoration = TextDecoration.Underline,
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.clickable(onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Login) {
									backStack.add(Screen.Settings.CustomHeaders)
									focusManager.clearFocus(true)
								}
							}
						})
				)

				Spacer(Modifier.weight(2.25f))
			}

			Column(
				modifier = Modifier
					.widthIn(max = 600.dp)
					.padding(horizontal = 16.dp)
					.padding(bottom = 8.dp)
			) {
				LoginScreenSyncStatus(loginUiState = loginState)
				Button(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						login()
					},
					enabled = !isBusy,
					shape = ContinuousCapsule
				) {
					Text(
						text = stringResource(Res.string.action_log_in),
						fontFamily = defaultFont(100)
					)
				}
			}
		}
	}

	if (localNetworkDenied) {
		FormDialog(
			onDismissRequest = { localNetworkDenied = false },
			icon = { Icon(Icons.Outlined.Error, null) },
			title = { Text(stringResource(Res.string.notice_local_network_denied)) },
			content = { Text(stringResource(Res.string.subtitle_local_network_denied)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						localNetworkDenied = false
						permissionManager.openPermissionsSettings()
					},
					shapes = SegmentedListButtonDefaults.shapes(index = 0, count = 1)
				) {
					Text(stringResource(Res.string.action_open_settings))
				}
			}
		)
	}

	if (showNotificationPermissionDialog) {
		NotificationPermissionDialog(
			onDismiss = {
				showNotificationPermissionDialog = false
				performLoginAction()
			},
			onAllow = {
				showNotificationPermissionDialog = false
				notificationManager.requestPermissions()
				performLoginAction()
			}
		)
	}
}
