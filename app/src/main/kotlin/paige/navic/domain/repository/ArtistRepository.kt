/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import paige.navic.database.dao.ArtistDao
import paige.navic.database.dao.LetterCount
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.paging.createPagedFlow
import paige.navic.database.query.getArtistLetterCountsQuery
import paige.navic.database.query.toSqlQuery
import paige.navic.domain.manager.session.SyncManager
import paige.navic.domain.model.DomainArtist
import paige.navic.domain.model.DomainArtistListType
import paige.navic.domain.model.DomainFilter
import kotlin.time.Clock

class ArtistRepository(
	private val artistDao: ArtistDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(
		listType: DomainArtistListType,
		filters: Set<DomainFilter> = emptySet(),
		reversed: Boolean = false,
	): Flow<PagingData<DomainArtist>> = createPagedFlow(
		isRandom = (listType == DomainArtistListType.Random),
		getRandomIds = {
			artistDao.getArtistIdsByQuery(
				listType.toSqlQuery(
					filters = filters,
					idsOnly = true
				)
			)
		},
		getEntityId = { it.artistId },
		fetchByIds = artistDao::getArtistsByIds,
		getStandardQuery = { artistDao.getArtistsByQuery(listType.toSqlQuery(reversed, filters)) },
		toDomain = { it.toDomainModel() }
	)

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

	suspend fun isArtistStarred(artistId: String) = artistDao.isArtistStarred(artistId)

	suspend fun starArtist(artistId: String) {
		artistDao.updateArtistStarredAt(artistId, Clock.System.now())
		syncManager.enqueueAction(SyncActionType.STAR, artistId)
	}

	suspend fun unstarArtist(artistId: String) {
		artistDao.updateArtistStarredAt(artistId, null)
		syncManager.enqueueAction(SyncActionType.UNSTAR, artistId)
	}

	fun getArtistLetterCounts(
		filters: Set<DomainFilter> = emptySet()
	): Flow<List<LetterCount>> {
		return artistDao.getArtistLetterCounts(getArtistLetterCountsQuery(filters))
	}
}
