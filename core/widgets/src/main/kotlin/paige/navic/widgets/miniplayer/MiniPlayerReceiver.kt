/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.widgets.miniplayer

import paige.navic.widgets.nowplaying.NowPlayingReceiver

class MiniPlayerReceiver : NowPlayingReceiver() {
	override val widgetClass = MiniPlayerWidget::class.java
}
