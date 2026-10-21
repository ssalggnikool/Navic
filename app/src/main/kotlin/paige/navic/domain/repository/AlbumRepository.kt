/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.LetterCount
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.paging.createPagedFlow
import paige.navic.database.query.getAlbumLetterCountsQuery
import paige.navic.database.query.toSqlQuery
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	suspend fun getAlbumById(id: String) = albumDao.getAlbumById(id)?.toDomainModel()

	private suspend fun getLocalData(
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainAlbum>> = createPagedFlow(
		isRandom = (listType == DomainAlbumListType.Random),
		getRandomIds = { albumDao.getAlbumIdsByQuery(listType.toSqlQuery(filters = filters, idsOnly = true)) },
		getEntityId = { it.album.albumId },
		fetchByIds = albumDao::getAlbumsByIds,
		getStandardQuery = { albumDao.getAlbumsByQuery(listType.toSqlQuery(reversed, filters)) },
		toDomain = { it.toDomainModel() }
	)

	private suspend fun refreshLocalData(
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainAlbum>> {
		dbRepository.syncLibrarySongs().getOrThrow()
		return getLocalData(listType, reversed, filters)
	}

	suspend fun getAlbums(
		fullRefresh: Boolean,
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainAlbum>> {
		val localData = getLocalData(listType, reversed, filters)
		return if (fullRefresh) {
			refreshLocalData(listType, reversed, filters)
		} else {
			localData
		}
	}

	suspend fun isAlbumStarred(albumId: String) = albumDao.isAlbumStarred(albumId)
	suspend fun getAlbumRating(albumId: String) = albumDao.getAlbumRating(albumId) ?: 0

	suspend fun starAlbum(albumId: String) {
		albumDao.updateAlbumStarredAt(albumId, Clock.System.now())
		syncManager.enqueueAction(SyncActionType.STAR, albumId)
	}

	suspend fun unstarAlbum(albumId: String) {
		albumDao.updateAlbumStarredAt(albumId, null)
		syncManager.enqueueAction(SyncActionType.UNSTAR, albumId)
	}

	suspend fun rateAlbum(albumId: String, rating: Int) {
		albumDao.updateAlbumRating(albumId, rating.takeIf { it > 0 })
		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, albumId)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, albumId)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, albumId)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, albumId)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, albumId)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, albumId)
		}
	}

	fun getAlbumLetterCounts(
		reversed: Boolean = false,
		filters: Set<DomainFilter> = emptySet()
	): Flow<List<LetterCount>> {
		return albumDao.getAlbumLetterCounts(getAlbumLetterCountsQuery(reversed, filters))
	}
}
