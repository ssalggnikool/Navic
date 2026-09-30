/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.data.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class DownloadEntity(
	@PrimaryKey val songId: String,
	val status: DownloadStatus,
	val progress: Float = 0f,
	val filePath: String? = null
)

@Serializable
enum class DownloadStatus {
	NOT_DOWNLOADED,
	DOWNLOADING,
	DOWNLOADED,
	FAILED
}
