/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.sync

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.dao.SyncActionDao
import paige.navic.database.entity.CreatePlaylistContent
import paige.navic.database.entity.PlaylistSongCrossRef
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toEntity
import paige.navic.domain.manager.session.SessionManager
import paige.navic.util.Logger

class SyncActionProcessor(
	private val syncDao: SyncActionDao,
	private val playlistDao: PlaylistDao,
	private val sessionManager: SessionManager
) {
	suspend fun processPending(): Result<Unit> {
		try {
			while (true) {
				val actions = syncDao.getPendingActions()
				if (actions.isEmpty()) return Result.success(Unit)

				for ((id, actionType, itemId, content, time) in actions) {
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

						SyncActionType.CREATE_PLAYLIST -> {
							val contentObj = content?.let {
								try {
									Json.decodeFromString<CreatePlaylistContent>(it)
								} catch (_: Exception) {
									null
								}
							}
							val name = contentObj?.name
							val songIds = contentObj?.songIds ?: emptyList()

							if (!name.isNullOrBlank()) {
								val remotePlaylist = sessionManager.api.createPlaylist(name, songIds)
								val remoteEntity = remotePlaylist.toEntity()

								playlistDao.deletePlaylist(itemId)
								playlistDao.deletePlaylistSongCrossRefs(itemId)

								playlistDao.insertPlaylist(remoteEntity)
								val remoteCrossRefs = if (remotePlaylist.songs.isNotEmpty()) {
									remotePlaylist.songs.mapIndexed { index, song ->
										PlaylistSongCrossRef(playlistId = remotePlaylist.id, songId = song.id, position = index)
									}
								} else {
									songIds.mapIndexed { index, songId ->
										PlaylistSongCrossRef(playlistId = remotePlaylist.id, songId = songId, position = index)
									}
								}
								playlistDao.insertPlaylistSongCrossRefs(remoteCrossRefs)

								syncDao.updateItemId(oldItemId = itemId, newItemId = remotePlaylist.id)
							}
						}
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
