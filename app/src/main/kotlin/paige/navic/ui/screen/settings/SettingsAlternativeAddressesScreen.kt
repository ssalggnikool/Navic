/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.ui.component.common.ContentUnavailable
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Add
import paige.navic.ui.icons.outlined.Delete
import paige.navic.ui.icons.outlined.Edit
import paige.navic.ui.screen.settings.component.SettingsGroup
import paige.navic.ui.screen.settings.component.SettingsGroupDefaults


@Composable
fun SettingsAlternativeAddressesScreen() {
	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	val alternativeAddresses = remember {
		preferenceManager.alternateServerAddresses.toMutableStateList()
	}

	val update = {
		preferenceManager.alternateServerAddresses = alternativeAddresses.filterNot { it.isBlank() }
		sessionManager.refreshClient()
	}

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(R.string.option_alternate_addresses)) }) },
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					alternativeAddresses.add("")
					update()
				},
				shape = MaterialTheme.shapes.medium
			) {
				Icon(
					imageVector = Icons.Outlined.Add,
					contentDescription = stringResource(R.string.action_new)
				)
			}
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			if (alternativeAddresses.isNotEmpty()) {
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
						alternativeAddresses.forEachIndexed { index, address ->
							AddressRow(
								value = address,
								onValueChanged = { newValue ->
									alternativeAddresses[index] = newValue
									update()
								},
								onDelete = {
									alternativeAddresses.removeAt(index)
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
private fun AddressRow(
	value: String,
	onValueChanged: (String) -> Unit,
	onDelete: () -> Unit
) {
	var isEditing by remember { mutableStateOf(false) }

	Surface {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = value.ifBlank { "(empty)" },
				modifier = Modifier.weight(1f)
			)
			IconButton(
				onClick = { isEditing = true }
			) {
				Icon(
					imageVector = Icons.Outlined.Edit,
					contentDescription = stringResource(R.string.action_edit)
				)
			}
			IconButton(
				onClick = onDelete
			) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = stringResource(R.string.action_delete),
					tint = MaterialTheme.colorScheme.error
				)
			}
		}
	}

	if (isEditing) {
		AlertDialog(
			onDismissRequest = { isEditing = false },
			title = { Text(stringResource(R.string.alt_addresses_edit_address)) },
			text = {
				Column {
					Text(stringResource(R.string.alt_addresses_dialog_hint))
					Spacer(modifier = Modifier.height(8.dp))
					OutlinedTextField(
						value = value,
						onValueChange = onValueChanged,
						placeholder = { Text(stringResource(R.string.alt_addresses_text_field_placeholder)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = { isEditing = false }
				) {
					Text(stringResource(R.string.action_ok))
				}
			}
		)
	}
}
