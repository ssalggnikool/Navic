/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.query.toSqlQuery
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainPlaylistListType

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository,
) {
	private fun getLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainPlaylist>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				playlistDao
					.getPlaylistsByQuery(listType.toSqlQuery(reversed, filters))
			}
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun refreshLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainPlaylist>> {
		dbRepository.syncPlaylists().getOrThrow().forEach { playlist ->
			dbRepository.syncPlaylistSongs(playlist.playlistId).getOrThrow()
		}
		return getLocalData(listType, reversed, filters)
	}

	suspend fun getPlaylists(
		fullRefresh: Boolean,
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainPlaylist>> {
		val localData = getLocalData(listType, reversed, filters)
		return if (fullRefresh) {
			refreshLocalData(listType, reversed, filters)
		} else {
			localData
		}
	}
}
