/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.query

import androidx.room3.RoomRawQuery

fun toRadioSqlQuery(): RoomRawQuery {
	val sql = "SELECT * FROM RadioEntity ORDER BY name ASC"
	return createRoomRawQuery(sql, emptyList())
}
