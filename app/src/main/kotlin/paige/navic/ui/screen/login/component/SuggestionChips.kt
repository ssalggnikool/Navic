/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.login.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paige.navic.R

@Composable
fun LoginScreenSuggestionChips(
	instanceState: TextFieldState,
	showSuggestions: Boolean = instanceState.text.isNotEmpty() &&
		!instanceState.text.startsWith("http://") &&
		!instanceState.text.startsWith("https://") &&
		(instanceState.text.contains(".") || instanceState.text.contains(":"))
) {
	AnimatedVisibility(
		modifier = Modifier.fillMaxWidth(),
		visible = showSuggestions,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(Modifier.height(4.dp))
			Text(
				text = stringResource(R.string.notice_login_suggestion),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(horizontal = 16.dp)
			)
			Spacer(Modifier.height(4.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState()),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				val url = instanceState.text.toString()
				Spacer(Modifier.width(8.dp))
				SuggestionChip(
					onClick = {
						instanceState.edit {
							replace(0, length, "https://$url")
						}
					},
					label = {
						Text(
							text = "https://${url.replace("https://", "").replace("http://", "")}",
							style = MaterialTheme.typography.labelSmall,
							maxLines = 1
						)
					}
				)
				SuggestionChip(
					onClick = {
						instanceState.edit {
							replace(0, length, "http://$url")
						}
					},
					label = {
						Text(
							text = "http://${url.replace("https://", "").replace("http://", "")}",
							style = MaterialTheme.typography.labelSmall,
							maxLines = 1
						)
					}
				)
				Spacer(Modifier.width(8.dp))
			}
		}
	}
}
