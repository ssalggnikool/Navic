/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.snackbars

import androidx.annotation.StringRes

data class PlayerEvent(
	@StringRes val resource: Int,
	val args: List<Any> = emptyList()
)
