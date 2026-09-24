package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_incorrect_proxy_url_format
import navic.composeapp.generated.resources.info_proxy_url_placeholder
import navic.composeapp.generated.resources.option_connection_options
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.option_ignore_ssl_certificates
import navic.composeapp.generated.resources.option_proxy_url
import navic.composeapp.generated.resources.subtitle_custom_headers
import navic.composeapp.generated.resources.subtitle_ignore_ssl_certificates
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalPlatformContext
import paige.navic.di.PlatformType
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem
import paige.navic.ui.screens.settings.components.SettingsToggleItem

@Composable
fun SettingsConnectionOptionsScreen() {
	val backStack = LocalNavStack.current
	val platformContext = LocalPlatformContext.current

	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	var proxyUrlField by mutableStateOf(preferenceManager.proxyUrl)
	val proxyUrlHasErrors by derivedStateOf {
		if (proxyUrlField.isNotBlank()) {
			!SessionManager.PROXY_URL_REGEX.matches(proxyUrlField)
		} else {
			false
		}
	}
	val updateProxyUrl: (String) -> Unit = { url ->
		proxyUrlField = url
		if (SessionManager.PROXY_URL_REGEX.matches(proxyUrlField)) {
			preferenceManager.proxyUrl = url
			sessionManager.refreshClient()
		}
	}

	var sslNoopChecked by mutableStateOf(preferenceManager.dangerousSslNoopEnabled)
	val updateSslNoop: (Boolean) -> Unit = { bool ->
		sslNoopChecked = bool
		preferenceManager.dangerousSslNoopEnabled = bool
		sessionManager.refreshClient()
	}

	val count = if (platformContext.platformType == PlatformType.Android) 2 else 1

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.option_connection_options)) }) }
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
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						value = proxyUrlField,
						onValueChange = updateProxyUrl,
						isError = proxyUrlHasErrors,
						label = { Text(stringResource(Res.string.option_proxy_url)) },
						placeholder = { Text(stringResource(Res.string.info_proxy_url_placeholder)) },
						supportingText = {
							if (proxyUrlHasErrors) {
								Text(stringResource(Res.string.info_incorrect_proxy_url_format))
							}
						}
					)
				}
				SettingsGroup {
					if (platformContext.platformType == PlatformType.Android) {
						SettingsToggleItem(
							checked = sslNoopChecked,
							onCheckedChange = updateSslNoop,
							content = { Text(stringResource(Res.string.option_ignore_ssl_certificates)) },
							supportingContent = { Text(stringResource(Res.string.subtitle_ignore_ssl_certificates)) },
							shapes = SegmentedListItemDefaults.segmentedShapes(
								index = 0,
								count = count
							)
						)
					}
					SettingsNavItem(
						content = { Text(stringResource(Res.string.option_custom_headers)) },
						supportingContent = { Text(stringResource(Res.string.subtitle_custom_headers)) },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = count
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
