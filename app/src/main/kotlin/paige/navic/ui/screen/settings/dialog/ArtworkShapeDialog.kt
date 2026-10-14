/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paige.navic.R
import paige.navic.domain.model.settings.CoverArtShape

@Composable
fun ArtworkShapeDialog(
	title: @Composable () -> Unit,
	selection: CoverArtShape,
	onSelect: (CoverArtShape) -> Unit,
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	AlertDialog(
		title = title,
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				CoverArtShape.entries.forEach { shape ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clip(MaterialTheme.shapes.small)
							.clickable {
								onSelect(shape)
								onDismissRequest()
							},
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = selection == shape,
							onClick = null
						)
						Box(
							modifier = Modifier
								.size(48.dp)
								.background(
									MaterialTheme.colorScheme.primaryContainer,
									shape.decreasedShape
								)
								.border(
									2.dp,
									MaterialTheme.colorScheme.primary,
									shape.decreasedShape
								)
						)
						Text(text = shape.name)
					}
				}
			}
		},
		onDismissRequest = onDismissRequest,
		confirmButton = {
			Button(onClick = {
				onDismissRequest()
			}) {
				Text(stringResource(R.string.action_ok))
			}
		}
	)
}
