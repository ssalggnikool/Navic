/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.model.DomainRadio
import paige.navic.ui.component.common.CoverArt
import paige.navic.ui.component.common.MarqueeText
import paige.navic.ui.icons.Icons
import paige.navic.ui.icons.outlined.Delete
import paige.navic.ui.icons.outlined.Edit

@Composable
fun RadioSheet(
	onDismissRequest: () -> Unit,
	radio: DomainRadio,
	onUpdateRadioRequest: () -> Unit,
	onDeleteRadioRequest: () -> Unit
) {
	val preferenceManager = koinInject<PreferenceManager>()

	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)
	val contentPadding = PaddingValues(horizontal = 16.dp)

	@OptIn(ExperimentalMaterial3Api::class)
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = null,
		containerColor = MaterialTheme.colorScheme.surface,
		sheetState = rememberBottomSheetState(
			initialValue = SheetValue.Hidden,
			enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
		),
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 8.dp,
					right = 8.dp
				)
			)
		}
	) {
		Spacer(Modifier.height(16.dp))

		ListItem(
			content = { MarqueeText(text = radio.name) },
			supportingContent = { MarqueeText(text = radio.streamUrl) },
			leadingContent = {
				CoverArt(
					coverArtId = radio.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = preferenceManager.coverArtShape.decreasedShape
				)
			},
			colors = colors
		)

		HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

		Column(Modifier.verticalScroll(rememberScrollState())) {
			ListItem(
				content = { Text(stringResource(R.string.action_edit)) },
				leadingContent = { Icon(Icons.Outlined.Edit, null) },
				onClick = onUpdateRadioRequest,
				colors = colors,
				contentPadding = contentPadding
			)
			ListItem(
				content = { Text(stringResource(R.string.action_delete)) },
				leadingContent = { Icon(Icons.Outlined.Delete, null) },
				onClick = onDeleteRadioRequest,
				colors = colors,
				contentPadding = contentPadding
			)
		}
	}
}
