/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class QueueInfoType(val displayName: Int) {
	Full(R.string.option_queue_info_type_full),
	Remaining(R.string.option_queue_info_type_remining);
}
