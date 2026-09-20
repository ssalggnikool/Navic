package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.info_incorrect_proxy_url_format
import navic.composeapp.generated.resources.info_proxy_url_placeholder
import navic.composeapp.generated.resources.option_connection_options
import navic.composeapp.generated.resources.option_custom_headers
import navic.composeapp.generated.resources.option_proxy_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Info
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsToggleItem
import paige.navic.ui.theme.defaultFont
import kotlin.random.Random

private data class Header(
	val id: Long = Random.nextLong(),
	val key: String,
	val value: String
)

@Composable
fun SettingsConnectionOptionsScreen() {
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

	val headers = remember {
		preferenceManager.customHeaders.lines()
			.filter { it.contains(":") }
			.map {
				val parts = it.split(":", limit = 2)
				Header(key = parts[0], value = parts[1])
			}
			.toMutableStateList()
	}

	val hiddenHeaders = remember { mutableStateSetOf<Long>() }

	fun updateSettings() {
		preferenceManager.customHeaders = headers
			.filter { !hiddenHeaders.contains(it.id) }
			.joinToString("\n") { "${it.key}:${it.value}" }
		sessionManager.refreshClient()
	}

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
				SettingsGroup(
					title = { Text(stringResource(Res.string.option_proxy_settings)) },
					modifier = Modifier.fillMaxWidth()
				) {
					OutlinedTextField(
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						value = proxyUrlField,
						onValueChange = updateProxyUrl,
						isError = proxyUrlHasErrors,
						label = { Text("URL") },
						placeholder = { Text(stringResource(Res.string.info_proxy_url_placeholder)) },
						supportingText = {
							if (proxyUrlHasErrors) {
								Text(stringResource(Res.string.info_incorrect_proxy_url_format))
							}
						}
					)
				}
				// TODO: hide this on iOS, and also add strings to the xml. piss
				SettingsGroup(
					title = { Text("Security settings") },
					modifier = Modifier.fillMaxWidth()
				) {
					SettingsToggleItem(
						checked = sslNoopChecked,
						onCheckedChange = updateSslNoop,
						content = { Text("Ignore SSL certificates") },
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = 1
						),
						supportingContent = {
							Text("Only enable this if you know what you're doing. Useful if you're using a proxy for inspecting requests.")
						}
					)
				}
				SettingsGroup(
					title = { Text(stringResource(Res.string.option_custom_headers)) },
					modifier = Modifier.fillMaxWidth()
				) {
					headers.forEachIndexed { index, header ->
						AnimatedVisibility(
							modifier = Modifier.fillMaxWidth(),
							visible = !hiddenHeaders.contains(header.id)
						) {
							HeaderRow(
								key = header.key,
								value = header.value,
								onSetKey = {
									headers[index] = header.copy(key = it)
									updateSettings()
								},
								onSetValue = {
									headers[index] = header.copy(value = it)
									updateSettings()
								},
								onDelete = {
									hiddenHeaders.add(header.id)
									updateSettings()
								}
							)
						}
					}
				}
				FilledTonalButton(
					onClick = {
						headers.add(Header(key = "", value = ""))
						updateSettings()
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(Icons.Outlined.Add, null)
					Spacer(Modifier.width(8.dp))
					Text(
						stringResource(Res.string.action_new),
						fontFamily = defaultFont(100)
					)
				}
			}
		}
	}
}

@Composable
private fun HeaderRow(
	key: String,
	value: String,
	onSetKey: (String) -> Unit,
	onSetValue: (String) -> Unit,
	onDelete: () -> Unit
) {
	Surface(
		color = MaterialTheme.colorScheme.surfaceContainer,
		shape = MaterialTheme.shapes.large
	) {
		Row(
			modifier = Modifier.padding(14.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				TextField(
					value = key,
					onValueChange = onSetKey,
					placeholder = { Text("Key") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					colors = TextFieldDefaults.colors(
						focusedIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent
					),
					shape = MaterialTheme.shapes.medium
				)
				TextField(
					value = value,
					onValueChange = onSetValue,
					placeholder = { Text("Value") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					colors = TextFieldDefaults.colors(
						focusedIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent
					),
					shape = MaterialTheme.shapes.medium
				)
			}
			FilledTonalButton(
				onClick = onDelete,
				contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
				shape = MaterialTheme.shapes.medium
			) {
				Icon(Icons.Outlined.Delete, stringResource(Res.string.action_delete))
			}
		}
	}
}
