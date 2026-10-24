/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalNavStack
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.ui.component.common.SegmentedListItemDefaults
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults
import paige.navic.ui.screen.settings.component.SettingsNavItem
import paige.navic.ui.screen.settings.component.SettingsToggleItem
import paige.navic.util.network.PROXY_URL_REGEX

@Composable
fun SettingsConnectionOptionsScreen() {
	val backStack = LocalNavStack.current

	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	var instanceUrlField by remember(sessionManager.instanceUrl) {
		mutableStateOf(sessionManager.instanceUrl)
	}
	val instanceUrlHasErrors by remember {
		derivedStateOf {
			instanceUrlField.isBlank()
		}
	}
	val applyInstanceUrl = {
		val trimmed = instanceUrlField.trim()
		if (trimmed.isNotBlank()) {
			sessionManager.updateInstanceUrl(trimmed)
			instanceUrlField = sessionManager.instanceUrl
		}
	}

	var proxyUrlField by remember {
		mutableStateOf(preferenceManager.proxyUrl)
	}
	val proxyUrlHasErrors by remember {
		derivedStateOf {
			proxyUrlField.isNotBlank() && !PROXY_URL_REGEX.matches(proxyUrlField)
		}
	}
	val updateProxyUrl: (String) -> Unit = { url ->
		proxyUrlField = url
		if (PROXY_URL_REGEX.matches(proxyUrlField) || proxyUrlField.isBlank()) {
			preferenceManager.proxyUrl = url
			sessionManager.refreshClient()
		}
	}

	var sslNoopChecked by remember {
		mutableStateOf(preferenceManager.dangerousSslNoopEnabled)
	}
	val updateSslNoop: (Boolean) -> Unit = { bool ->
		sslNoopChecked = bool
		preferenceManager.dangerousSslNoopEnabled = bool
		sessionManager.refreshClient()
	}

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.option_connection_options)) }) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp),
				verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
			) {
				SettingsGroup {
					OutlinedTextField(
						modifier = Modifier
							.fillMaxWidth()
							.onFocusChanged { focusState ->
								if (!focusState.isFocused) {
									applyInstanceUrl()
								}
							},
						singleLine = true,
						value = instanceUrlField,
						onValueChange = { newValue ->
							instanceUrlField = newValue
							val trimmed = newValue.trim()
							if (trimmed.isNotBlank() && (trimmed.startsWith("http://") || trimmed.startsWith(
									"https://"
								))
							) {
								sessionManager.updateInstanceUrl(trimmed)
							}
						},
						isError = instanceUrlHasErrors,
						label = { Text(stringResource(R.string.option_account_instance)) },
						placeholder = { Text(stringResource(R.string.option_account_instance)) },
						keyboardOptions = KeyboardOptions(
							autoCorrectEnabled = false,
							keyboardType = KeyboardType.Uri,
							imeAction = ImeAction.Done
						),
						keyboardActions = KeyboardActions(
							onDone = { applyInstanceUrl() }
						),
						supportingText = {
							if (instanceUrlHasErrors) {
								Text(stringResource(R.string.info_incorrect_instance_url_format))
							}
						}
					)
					OutlinedTextField(
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						value = proxyUrlField,
						onValueChange = updateProxyUrl,
						isError = proxyUrlHasErrors,
						label = { Text(stringResource(R.string.option_proxy_url)) },
						placeholder = { Text(stringResource(R.string.info_proxy_url_placeholder)) },
						supportingText = {
							if (proxyUrlHasErrors) {
								Text(stringResource(R.string.info_incorrect_proxy_url_format))
							}
						}
					)
				}
				SettingsGroup {
					SettingsNavItem(
						content = { Text(stringResource(R.string.option_alternate_addresses)) },
						supportingContent = { Text(stringResource(R.string.subtitle_alternative_addresses)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 0,
							count = 3
						),
						onClick = dropUnlessResumed {
							if (Screen.Settings.AlternativeAddresses !in backStack) {
								backStack.add(Screen.Settings.AlternativeAddresses)
							}
						}
					)
					SettingsToggleItem(
						checked = sslNoopChecked,
						onCheckedChange = updateSslNoop,
						content = { Text(stringResource(R.string.option_ignore_ssl_certificates)) },
						supportingContent = { Text(stringResource(R.string.subtitle_ignore_ssl_certificates)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = 3
						)
					)
					SettingsNavItem(
						content = { Text(stringResource(R.string.option_custom_headers)) },
						supportingContent = { Text(stringResource(R.string.subtitle_custom_headers)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 2,
							count = 3
						),
						onClick = dropUnlessResumed {
							if (Screen.Settings.CustomHeaders !in backStack) {
								backStack.add(Screen.Settings.CustomHeaders)
							}
						}
					)
				}
			}
		}
	}
}
