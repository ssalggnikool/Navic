/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.snackbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NavicSnackBar(
	snackBarData: SnackbarData,
	modifier: Modifier = Modifier
) {
	Snackbar(
		modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
		snackbarData = snackBarData,
		shape = MaterialTheme.shapes.large,
		containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
		contentColor = MaterialTheme.colorScheme.onSurface,
		actionColor = MaterialTheme.colorScheme.primary,
		actionContentColor = MaterialTheme.colorScheme.primary
	)
}
