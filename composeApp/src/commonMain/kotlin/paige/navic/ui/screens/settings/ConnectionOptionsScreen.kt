package paige.navic.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.info_incorrect_proxy_url_format
import navic.composeapp.generated.resources.info_proxy_url_placeholder
import navic.composeapp.generated.resources.option_connection_options
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalPlatformContext
import paige.navic.di.PlatformType
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Edit
import paige.navic.ui.components.common.SegmentedListItemDefaults
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults
import paige.navic.ui.screens.settings.components.SettingsNavItem
import paige.navic.ui.screens.settings.components.SettingsToggleItem
import paige.navic.ui.theme.defaultFont

private data class Header(
	val key: String,
	val value: String
)

@Composable
fun SettingsConnectionOptionsScreen() {
	val platformContext = LocalPlatformContext.current

	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	var showHeadersBottomSheet by remember { mutableStateOf(false) }

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

	fun updateSettings() {
		preferenceManager.customHeaders = headers
			.filter { it.key.isNotBlank() && it.value.isNotBlank() }
			.joinToString("\n") { "${it.key}:${it.value}" }
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
				SettingsGroup(
					modifier = Modifier.fillMaxWidth()
				) {
					OutlinedTextField(
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						value = proxyUrlField,
						onValueChange = updateProxyUrl,
						isError = proxyUrlHasErrors,
						label = { Text("Proxy URL") },
						placeholder = { Text(stringResource(Res.string.info_proxy_url_placeholder)) },
						supportingText = {
							if (proxyUrlHasErrors) {
								Text(stringResource(Res.string.info_incorrect_proxy_url_format))
							}
						}
					)
				}
				SettingsGroup(
					modifier = Modifier.fillMaxWidth()
				) {
					if (platformContext.platformType == PlatformType.Android) {
						SettingsToggleItem(
							checked = sslNoopChecked,
							onCheckedChange = updateSslNoop,
							content = { Text("Ignore SSL certificates") },
							shapes = SegmentedListItemDefaults.segmentedShapes(
								index = 0,
								count = count
							),
							supportingContent = {
								Text("Only enable this if you know what you're doing. Useful if you're using a proxy for inspecting requests.")
							}
						)
					}
					SettingsNavItem(
						content = { Text("Custom headers") },
						supportingContent = {
							Text("Additional information attached to each request")
						},
						shapes = SegmentedListItemDefaults.segmentedShapes(
							index = 1,
							count = count
						),
						onClick = {
							showHeadersBottomSheet = !showHeadersBottomSheet
						}
					)
				}
			}

			if (showHeadersBottomSheet) {
				CustomHeadersBottomSheet(
					headers,
					onUpdate = {
						updateSettings()
					},
					onDismiss = {
						showHeadersBottomSheet = !showHeadersBottomSheet
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomHeadersBottomSheet(
	headers: SnapshotStateList<Header>,
	onUpdate: () -> Unit,
	onDismiss: () -> Unit
) {
	ModalBottomSheet(
		onDismissRequest = onDismiss
	) {
		Column(
			modifier = Modifier.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						modifier = Modifier.padding(4.dp),
						text = "Custom headers",
						style = MaterialTheme.typography.headlineMediumEmphasized
					)
					Text(
						text = "Additional information attached to each request",
						style = MaterialTheme.typography.bodyMedium,
						textAlign = TextAlign.Center
					)
				}
				FilledTonalButton(
					onClick = {
						if (headers.none { it.key.isBlank() }) {
							headers.add(Header(key = "", value = ""))
						}
						onUpdate()
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
			SettingsGroup(
				modifier = Modifier.verticalScroll(rememberScrollState())
					.weight(weight = 1f, fill = false)
					.animateContentSize(),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				headers.forEachIndexed { index, header ->
					HeaderRow(
						key = header.key,
						value = header.value,
						onSetKey = {
							headers[index] = header.copy(key = it)
							onUpdate()
						},
						onSetValue = {
							headers[index] = header.copy(value = it)
							onUpdate()
						},
						onDelete = {
							headers.remove(header)
							onUpdate()
						}
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
	var isEditing by remember { mutableStateOf(key.isBlank()) }

	val focusRequester = remember { FocusRequester() }
	val enterEditMode: () -> Unit = {
		isEditing = !isEditing
		focusRequester.captureFocus()
	}
	val onFocus: (FocusState) -> Unit = {
		if (!it.hasFocus && !it.isCaptured) {
			isEditing = false
		}
	}

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
				modifier = Modifier.weight(1f)
					.focusRequester(focusRequester)
					.onFocusChanged(onFocus)
					.animateContentSize(),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				if (isEditing) {
					TextField(
						value = key,
						onValueChange = onSetKey,
						placeholder = { Text("Key") },
						modifier = Modifier.fillMaxWidth()
							.heightIn(max = 54.dp),
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
						modifier = Modifier.fillMaxWidth()
							.heightIn(max = 54.dp),
						singleLine = true,
						colors = TextFieldDefaults.colors(
							focusedIndicatorColor = Color.Transparent,
							unfocusedIndicatorColor = Color.Transparent
						),
						shape = MaterialTheme.shapes.medium
					)
				} else {
					Text(
						text = key
					)
				}
			}
			Column(
				verticalArrangement = Arrangement.SpaceBetween
			) {
				val padding = Modifier.padding(vertical = 8.dp)

				if (isEditing) {
					IconButton(
						modifier = padding,
						onClick = onDelete,
						shape = MaterialTheme.shapes.medium
					) {
						Icon(
							Icons.Outlined.Delete,
							stringResource(Res.string.action_delete)
						)
					}
					IconButton(
						modifier = padding,
						onClick = { isEditing = !isEditing },
						shape = MaterialTheme.shapes.medium
					) {
						Icon(
							Icons.Outlined.Check,
							"Finish editing"
						)
					}
				} else {
					IconButton(
						modifier = padding,
						onClick = enterEditMode,
						shape = MaterialTheme.shapes.medium
					) {
						Icon(
							Icons.Outlined.Edit,
							"Edit"
						)
					}
				}
			}
		}
	}
}
