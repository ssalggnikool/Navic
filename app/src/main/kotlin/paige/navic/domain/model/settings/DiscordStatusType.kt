/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import night.milkyway.antisocialcord.model.StatusDisplayType
import paige.navic.R

enum class DiscordStatusType(
	val displayName: Int,
	val wrappedType: StatusDisplayType
) {
	ArtistName(R.string.option_discord_rpc_status_type_artist_name, StatusDisplayType.STATE),
	SongName(R.string.option_discord_rpc_status_type_song_name, StatusDisplayType.DETAILS),
	AppName(R.string.option_discord_rpc_status_type_app_name, StatusDisplayType.NAME)
}
