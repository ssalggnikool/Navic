/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class DownloadStorage(val displayName: Int) {
	Internal(R.string.option_download_storage_internal),
	External(R.string.option_download_storage_external),
}
