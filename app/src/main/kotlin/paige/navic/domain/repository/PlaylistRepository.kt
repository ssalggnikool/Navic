/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import paige.navic.database.dao.PlaylistDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.paging.IdArrayPagingSource
import paige.navic.database.query.toSqlQuery
import paige.navic.database.relation.PlaylistWithSongs
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainPlaylist
import paige.navic.domain.model.DomainPlaylistListType

class PlaylistRepository(
	private val playlistDao: PlaylistDao,
	private val dbRepository: DbRepository,
) {
	private suspend fun getLocalData(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainPlaylist>> {
		val pagingSourceFactory = when (listType) {
			DomainPlaylistListType.Random -> randomPagingSourceFactory(listType, filters)
			else -> playlistsPagingSourceFactory(listType, reversed, filters)
		}

		return Pager(
			config = PagingConfig(pageSize = 20, maxSize = 100),
			pagingSourceFactory = pagingSourceFactory
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun randomPagingSourceFactory(
		listType: DomainPlaylistListType,
		filters: Set<DomainFilter>
	): () -> PagingSource<Int, PlaylistWithSongs> {
		val shuffledIds = withContext(Dispatchers.Default) {
			playlistDao.getPlaylistIdsByQuery(listType.toSqlQuery(filters = filters, idsOnly = true)).shuffled()
		}
		return {
			IdArrayPagingSource(
				ids = shuffledIds,
				getId = { it.playlist.playlistId },
				fetchByIds = playlistDao::getPlaylistsByIds
			)
		}
	}

	private fun playlistsPagingSourceFactory(
		listType: DomainPlaylistListType,
		reversed: Boolean,
		filters: Set<DomainFilter>
	): () -> PagingSource<Int, PlaylistWithSongs> {
		val query = listType.toSqlQuery(reversed, filters)
		return { playlistDao.getPlaylistsByQuery(query) }
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
