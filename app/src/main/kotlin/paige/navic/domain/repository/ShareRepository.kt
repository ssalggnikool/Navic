/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.manager.SessionManager

class ShareRepository(
	private val sessionManager: SessionManager
) {
	suspend fun getShares() = sessionManager.api.getShares().map { it.toDomainModel() }
}
