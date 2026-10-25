/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.DownloadDeletionType
import paige.navic.ui.component.sheet.ModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDownloadDeletionSheet(
	presented: Boolean,
	onDismissRequest: () -> Unit,
) {
	if (!presented) return

	val preferenceManager = koinInject<PreferenceManager>()

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = {
			Surface(
				modifier = Modifier.padding(vertical = 6.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				shape = ContinuousCapsule,
			) {
				Box(Modifier.size(width = 32.dp, height = 4.dp))
			}
		},
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					top = 16.dp,
					left = 12.dp,
					right = 12.dp,
					bottom = 8.dp
				)
			)
		}
	) {
		Column(
			modifier = Modifier.padding(horizontal = 16.dp)
		) {
			Text(
				text = stringResource(R.string.option_confirm_download_deletion),
				style = MaterialTheme.typography.titleLarge
			)

			Spacer(Modifier.height(16.dp))

			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				DownloadDeletionType.entries.forEach { type ->
					DeletionTypeRow(
						type = type,
						preferenceManager = preferenceManager
					)
				}
			}

			Spacer(Modifier.height(16.dp))
		}
	}
}

@Composable
private fun DeletionTypeRow(
	type: DownloadDeletionType,
	preferenceManager: PreferenceManager
) {
	var isChecked by remember(type) {
		mutableStateOf(preferenceManager.isConfirmDeleteEnabled(type))
	}

	val onToggle = {
		preferenceManager.toggleConfirmDelete(type)
		isChecked = !isChecked
	}

	val labelRes = when (type) {
		DownloadDeletionType.SONG -> R.string.title_songs
		DownloadDeletionType.ALBUM -> R.string.title_albums
		DownloadDeletionType.ARTIST -> R.string.title_artists
		DownloadDeletionType.PLAYLIST -> R.string.title_playlists
		DownloadDeletionType.LIBRARY -> R.string.title_library
	}

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.large)
			.clickable(onClick = onToggle),
		shape = MaterialTheme.shapes.large,
		color = MaterialTheme.colorScheme.surfaceContainer
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				checked = isChecked,
				onCheckedChange = { onToggle() }
			)
			Spacer(Modifier.width(12.dp))
			Text(stringResource(labelRes))
		}
	}
}
