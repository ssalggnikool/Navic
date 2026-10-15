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
import paige.navic.database.dao.ArtistDao
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.database.query.toSqlQuery
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter
import kotlin.time.Clock

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private fun getLocalData(
		listType: DomainArtistListType,
		filters: Set<DomainFilter> = emptySet(),
		reversed: Boolean = false,
	): Flow<PagingData<DomainArtist>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				artistDao
					.getArtistsByQuery(listType.toSqlQuery(reversed, filters))
			}
			).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun refreshLocalData(
		listType: DomainArtistListType,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainArtist>> {
		dbRepository.syncArtists().getOrThrow()
		return getLocalData(listType, filters)
	}

	suspend fun getArtists(
		fullRefresh: Boolean,
		listType: DomainArtistListType,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainArtist>> {
		val localData = getLocalData(listType, filters)
		return if (fullRefresh) {
			refreshLocalData(listType, filters)
		} else {
			localData
		}
	}

	suspend fun isArtistStarred(artist: DomainArtist) = artistDao.isArtistStarred(artist.id)

	suspend fun starArtist(artist: DomainArtist) {
		val starredEntity = artist.toEntity().copy(
			starredAt = Clock.System.now()
		)
		artistDao.insertArtist(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, artist.id)
	}

	suspend fun unstarArtist(artist: DomainArtist) {
		val unstarredEntity = artist.toEntity().copy(
			starredAt = null
		)
		artistDao.insertArtist(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, artist.id)
	}
}
