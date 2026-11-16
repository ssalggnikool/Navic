/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.widgets.turntable

import paige.navic.widgets.nowplaying.NowPlayingReceiver

class TurnTableReceiver : NowPlayingReceiver(TurnTableWidget::class.java)
