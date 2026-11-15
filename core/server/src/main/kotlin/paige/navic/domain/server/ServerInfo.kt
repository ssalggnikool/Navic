/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.server

enum class ServerExtension(
	val prettyName: String,
	val serverType: ServerType
) {
	OPENSUBSONIC_TRANSCODE_DECISION(
		prettyName = "Transcode decision",
		serverType = ServerType.SUBSONIC
	),
	OPENSUBSONIC_TRANSCODE_OFFSET(
		prettyName = "Transcode time offset",
		serverType = ServerType.SUBSONIC
	),
	OPENSUBSONIC_PLAYBACK_REPORT(
		prettyName = "Playback report",
		serverType = ServerType.SUBSONIC
	),
	SUBSONIC_CHAT(
		prettyName = "Chat",
		serverType = ServerType.SUBSONIC
	)
}

enum class ServerType(
	val prettyName: String
) {
	SUBSONIC("Subsonic")
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
