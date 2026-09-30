/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import paige.navic.R

enum class ExplicitContentPlayback(val displayName: Int) {
	Allowed(R.string.option_explicit_playback_allowed),
	Skip(R.string.option_explicit_playback_skip),
	SkipForThisSession(R.string.option_explicit_playback_skip_session)
}
