/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

// Which equalizer processes Navic's audio session. Builtin/External need to be mutually exclusive,
// otherwise they will fight for effect control and cause audio issues
enum class EqualizerMode {
	Disabled,
	BuiltIn,
	External
}
