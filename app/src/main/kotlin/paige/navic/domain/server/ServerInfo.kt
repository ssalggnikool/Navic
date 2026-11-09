/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

enum class ServerExtension {
	TRANSCODE_DECISION,
	CHAT
}

data class ServerStatus(
	val serverVersion: String?,
	val version: String?,
	val type: String?,
	val isOpenSubsonic: Boolean
)

data class ServerInfo(
	val extensions: Set<ServerExtension> = emptySet(),
	val serverVersion: String? = null,
	val version: String? = null,
	val type: String? = null,
	val isOpenSubsonic: Boolean = false
)
