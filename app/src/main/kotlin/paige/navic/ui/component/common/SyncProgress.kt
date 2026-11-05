/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun SyncProgressBar(
	modifier: Modifier = Modifier,
	visible: Boolean,
	progress: Float? = null
) {
	val smoothedProgress by animateFloatAsState(
		(progress ?: 0f).coerceIn(0f..1f),
		animationSpec = tween(
			durationMillis = 250,
			easing = EaseOut
		)
	)
	AnimatedVisibility(
		modifier = modifier.fillMaxWidth(),
		visible = visible,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		if (progress != null) {
			LinearWavyProgressIndicator(
				modifier = Modifier.fillMaxWidth(),
				progress = { smoothedProgress }
			)
		} else {
			LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
		}
	}
}
