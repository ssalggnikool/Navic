/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.sync

import kotlinx.coroutines.CancellationException
import paige.navic.database.dao.SyncActionDao
import paige.navic.database.entity.SyncActionType
import paige.navic.domain.manager.SessionManager
import paige.navic.util.Logger

class SyncActionProcessor(
	private val syncDao: SyncActionDao,
	private val sessionManager: SessionManager
) {
	suspend fun processPending(): Result<Unit> {
		try {
			while (true) {
				val actions = syncDao.getPendingActions()
				if (actions.isEmpty()) return Result.success(Unit)

				for ((id, actionType, itemId, time) in actions) {
					when (actionType) {
						SyncActionType.STAR -> sessionManager.api.star(itemId)
						SyncActionType.UNSTAR -> sessionManager.api.unstar(itemId)
						SyncActionType.DELETE_PLAYLIST ->
							sessionManager.api.deletePlaylist(itemId)

						SyncActionType.SCROBBLE -> sessionManager.api.scrobble(
							itemId,
							submission = true,
							time = time
						)

						SyncActionType.STAR_0 -> sessionManager.api.setRating(itemId, 0)
						SyncActionType.STAR_1 -> sessionManager.api.setRating(itemId, 1)
						SyncActionType.STAR_2 -> sessionManager.api.setRating(itemId, 2)
						SyncActionType.STAR_3 -> sessionManager.api.setRating(itemId, 3)
						SyncActionType.STAR_4 -> sessionManager.api.setRating(itemId, 4)
						SyncActionType.STAR_5 -> sessionManager.api.setRating(itemId, 5)
					}

					syncDao.removeAction(id)
					Logger.i(
						"SyncActionProcessor",
						"Successfully synced $actionType for $itemId"
					)
				}
			}
		} catch (e: CancellationException) {
			throw e
		} catch (e: Exception) {
			Logger.e("SyncActionProcessor", "Network failed. Action left in queue.", e)
			return Result.failure(e)
		}
	}
}
