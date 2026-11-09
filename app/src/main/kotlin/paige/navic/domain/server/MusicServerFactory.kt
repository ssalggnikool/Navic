/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

fun interface MusicServerFactory {
	fun create(baseUrl: String, username: String, password: String): MusicServer
}
