package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_edit
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.option_custom_headers
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Edit
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingsGroup
import paige.navic.ui.screens.settings.components.SettingsGroupDefaults

private data class Header(
	val key: String,
	val value: String
)

@Composable
fun SettingsCustomHeadersScreen() {
	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	val headers = remember {
		preferenceManager.customHeaders.lines()
			.filter { it.contains(":") }
			.map {
				val parts = it.split(":", limit = 2)
				Header(key = parts[0], value = parts[1])
			}
			.toMutableStateList()
	}

	val update = {
		preferenceManager.customHeaders = headers
			.filter { it.key.isNotBlank() && it.value.isNotBlank() }
			.joinToString("\n") { "${it.key}:${it.value}" }
		sessionManager.refreshClient()
	}

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.option_custom_headers)) }) },
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					headers.add(Header(key = "", value = ""))
					update()
				},
				shape = MaterialTheme.shapes.medium
			) {
				Icon(
					imageVector = Icons.Outlined.Add,
					contentDescription = stringResource(Res.string.action_new)
				)
			}
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			if (headers.isNotEmpty()) {
				Column(
					modifier = Modifier
						.padding(innerPadding)
						.verticalScroll(rememberScrollState())
						.padding(horizontal = 16.dp),
					verticalArrangement = Arrangement.spacedBy(SettingsGroupDefaults.GapBetweenGroups)
				) {
					SettingsGroup(
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						HorizontalDivider()
						headers.forEachIndexed { index, header ->
							HeaderRow(
								key = header.key,
								onKeyChanged = {
									headers[index] = header.copy(key = it)
									update()
								},
								value = header.value,
								onValueChanged = {
									headers[index] = header.copy(value = it)
									update()
								},
								onDelete = {
									headers.remove(header)
									update()
								}
							)
							HorizontalDivider()
						}
					}
				}
			} else {

				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					verticalArrangement = Arrangement.Center
				) {
					ContentUnavailable()
				}
			}
		}
	}
}

@Composable
private fun HeaderRow(
	key: String,
	onKeyChanged: (String) -> Unit,
	value: String,
	onValueChanged: (String) -> Unit,
	onDelete: () -> Unit
) {
	val focusRequester = remember { FocusRequester() }

	var keyIsFocused by remember { mutableStateOf(false) }
	var valIsFocused by remember { mutableStateOf(false) }

	var isEditing by remember { mutableStateOf(false) }

	LaunchedEffect(keyIsFocused, valIsFocused) {
		if (!keyIsFocused && !valIsFocused) {
			isEditing = false
		}
	}

	Surface {
		Column {
			if (isEditing) {
				LaunchedEffect(Unit) {
					if (!keyIsFocused && !valIsFocused) {
						focusRequester.requestFocus()
					}
				}
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					OutlinedTextField(
						value = key,
						onValueChange = onKeyChanged,
						label = { Text("Key") },
						singleLine = true,
						modifier = Modifier
							.weight(1f)
							.focusRequester(focusRequester)
							.onFocusChanged { keyIsFocused = it.hasFocus }
					)
					IconButton(
						// why the fuck is this not centered dude im gonna lose my mind
						modifier = Modifier.offset(y = 3.dp),
						onClick = onDelete
					) {
						Icon(
							imageVector = Icons.Outlined.Delete,
							contentDescription = stringResource(Res.string.action_delete)
						)
					}
				}
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					OutlinedTextField(
						value = value,
						onValueChange = onValueChanged,
						label = { Text("Value") },
						singleLine = true,
						modifier = Modifier
							.weight(1f)
							.onFocusChanged { valIsFocused = it.hasFocus }
					)
					Spacer(Modifier.size(IconButtonDefaults.smallContainerSize()))
				}
			} else {
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = key,
						modifier = Modifier.weight(1f)
					)
					IconButton(
						onClick = { isEditing = true }
					) {
						Icon(
							imageVector = Icons.Outlined.Edit,
							contentDescription = stringResource(Res.string.action_edit)
						)
					}
				}
			}
		}
	}
}
