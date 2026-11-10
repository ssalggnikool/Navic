/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSnackBarState
import paige.navic.domain.manager.runtime.PermissionManager
import paige.navic.domain.manager.session.LoginManager
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.common.SegmentedListButton
import paige.navic.ui.component.dialog.FormDialog
import paige.navic.ui.core.LoginEvent
import paige.navic.ui.core.LoginField
import paige.navic.ui.core.UiState
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Error
import paige.navic.ui.navigation.Screen
import paige.navic.ui.navigation.Screen.Library
import paige.navic.ui.theme.defaultFont

@Composable
fun LoginScreenContent(innerPadding: PaddingValues) {
	val viewModel = koinInject<LoginManager>()
	val formState by viewModel.formState.collectAsStateWithLifecycle()

	val instanceState = viewModel.instanceState
	val usernameState = viewModel.usernameState
	val passwordState = viewModel.passwordState

	var errorState by remember { mutableStateOf<Exception?>(null) }
	var isBusy by remember { mutableStateOf(false) }

	val snackBarState = LocalSnackBarState.current
	val resources = LocalResources.current

	val haptics = LocalHapticFeedback.current
	val backStack = LocalNavStack.current
	val focusManager = LocalFocusManager.current

	val instanceFocusRequester = remember { FocusRequester() }
	val usernameFocusRequester = remember { FocusRequester() }
	val passwordFocusRequester = remember { FocusRequester() }

	val permissionManager = koinInject<PermissionManager>()
	val loginScope = rememberCoroutineScope()
	var localNetworkDenied by rememberSaveable { mutableStateOf(false) }
	var notificationsDenied by rememberSaveable { mutableStateOf(false) }

	val login: () -> Unit = {
		loginScope.launch {
			if (viewModel.isLocalNetworkInstance()) {
				if (!permissionManager.requestLocalNetworkPermission()) {
					localNetworkDenied = true
					return@launch
				}
			}
			if (!permissionManager.requestNotificationsPermission()) {
				notificationsDenied = true
				return@launch
			}
			viewModel.login()
		}
	}

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			isBusy = event is LoginEvent.Loading

			when (event) {
				LoginEvent.Success -> {
					backStack.clear()
					backStack.add(Library())
				}

				LoginEvent.InvalidCredentials -> {
					snackBarState.showSnackbar(
						resources.getString(R.string.info_incorrect_credentials)
					)
				}

				is LoginEvent.ValidationFailed -> {
					haptics.performHapticFeedback(HapticFeedbackType.Reject)
					when (event.field) {
						LoginField.Instance -> instanceFocusRequester.requestFocus()
						LoginField.Username -> usernameFocusRequester.requestFocus()
						LoginField.Password -> passwordFocusRequester.requestFocus()
					}
				}

				is LoginEvent.Error -> {
					errorState = event.error
				}
				else -> {}
			}
		}
	}

	Box {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.consumeWindowInsets(innerPadding)
				.imePadding(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Spacer(
				Modifier.weight(0.2f)
			)
			Column(
				modifier = Modifier
					.weight(1f)
					.verticalScroll(rememberScrollState())
			) {
				Text(
					text = stringResource(R.string.action_log_in),
					style = MaterialTheme.typography.headlineMedium,
					fontFamily = defaultFont(round = 100f),
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				Text(
					text = stringResource(R.string.info_login_description),
					modifier = Modifier.padding(horizontal = 16.dp)
				)

				Spacer(Modifier.height(8.dp))

				val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
				val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
				AnimatedContent(
					errorState != null,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp),
					transitionSpec = {
						(fadeIn(
							animationSpec = effectSpec
						) + scaleIn(
							initialScale = 0.8f,
							animationSpec = spatialSpec
						)) togetherWith (fadeOut(
							animationSpec = effectSpec
						) + scaleOut(
							animationSpec = spatialSpec
						))
					}
				) {
					if (it) {
						errorState?.let { ex ->
							ErrorBox(
								UiState.Error(ex, null),
								padding = PaddingValues(0.dp),
								modifier = Modifier.fillMaxWidth(),
								bottomPadding = 8.dp
							)
						}
					}
				}

				LoginScreenFields(
					isBusy = isBusy,
					instanceState = instanceState,
					instanceError = formState.instanceError,
					instanceFocusRequester = instanceFocusRequester,
					authMethod = viewModel.authMethod,
					onAuthMethodChange = viewModel::selectAuthMethod,
					usernameState = usernameState,
					usernameError = formState.usernameError,
					usernameFocusRequester = usernameFocusRequester,
					passwordState = passwordState,
					passwordError = formState.passwordError,
					passwordFocusRequester = passwordFocusRequester,
					onLogin = login
				)

				Spacer(Modifier.height(12.dp))

				Text(
					text = stringResource(R.string.option_connection_options),
					color = MaterialTheme.colorScheme.primary,
					textDecoration = TextDecoration.Underline,
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.clickable(onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Login) {
									backStack.add(Screen.Settings.ConnectionOptions)
									focusManager.clearFocus(true)
								}
							}
						})
				)
			}

			Column(
				modifier = Modifier
					.widthIn(max = 600.dp)
					.padding(horizontal = 16.dp)
					.padding(bottom = 8.dp)
			) {
				Button(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						login()
					},
					enabled = !isBusy,
					shape = CircleShape
				) {
					Text(
						text = stringResource(R.string.action_log_in),
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
			title = { Text(stringResource(R.string.notice_local_network_denied)) },
			content = { Text(stringResource(R.string.subtitle_local_network_denied)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						localNetworkDenied = false
						permissionManager.openPermissionsSettings()
					}
				) {
					Text(stringResource(R.string.action_open_settings))
				}
			}
		)
	}

	if (notificationsDenied) {
		FormDialog(
			onDismissRequest = { notificationsDenied = false },
			icon = { Icon(Icons.Outlined.Error, null) },
			title = { Text(stringResource(R.string.notice_notifications_denied)) },
			content = { Text(stringResource(R.string.subtitle_notifications_denied)) },
			buttons = {
				SegmentedListButton(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						notificationsDenied = false
						permissionManager.openPermissionsSettings()
					}
				) {
					Text(stringResource(R.string.action_open_settings))
				}
			}
		)
	}
}
