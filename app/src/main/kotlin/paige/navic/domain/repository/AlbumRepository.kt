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
import paige.navic.database.dao.AlbumDao
import paige.navic.database.dao.LetterCount
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.database.paging.IdArrayPagingSource
import paige.navic.database.query.getAlbumLetterCountsQuery
import paige.navic.database.query.toSqlQuery
import paige.navic.database.relation.AlbumWithSongs
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
	): Flow<PagingData<DomainAlbum>> {
		val pagingSourceFactory = when (listType) {
			DomainAlbumListType.Random -> randomPagingSourceFactory(listType, filters)
			else -> albumsPagingSourceFactory(listType, reversed, filters)
		}

		return Pager(
			config = PagingConfig(pageSize = 20, maxSize = 100),
			pagingSourceFactory = pagingSourceFactory
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun randomPagingSourceFactory(
		listType: DomainAlbumListType,
		filters: Set<DomainFilter>
	): () -> PagingSource<Int, AlbumWithSongs> {
		val shuffledIds = withContext(Dispatchers.Default) {
			albumDao.getAlbumIdsByQuery(listType.toSqlQuery(filters = filters, idsOnly = true)).shuffled()
		}
		return {
			IdArrayPagingSource(
				ids = shuffledIds,
				getId = { it.album.albumId },
				fetchByIds = albumDao::getAlbumsByIds
			)
		}
	}

	private fun albumsPagingSourceFactory(
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter>
	): () -> PagingSource<Int, AlbumWithSongs> {
		val query = listType.toSqlQuery(reversed, filters)
		return { albumDao.getAlbumsByQuery(query) }
	}

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

	suspend fun isAlbumStarred(album: DomainAlbum) = albumDao.isAlbumStarred(album.id)
	suspend fun getAlbumRating(album: DomainAlbum) = albumDao.getAlbumRating(album.id) ?: 0

	suspend fun starAlbum(album: DomainAlbum) {
		val starredEntity = album.toEntity().copy(
			starredAt = Clock.System.now()
		)
		albumDao.insertAlbum(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, album.id)
	}

	suspend fun unstarAlbum(album: DomainAlbum) {
		val unstarredEntity = album.toEntity().copy(
			starredAt = null
		)
		albumDao.insertAlbum(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, album.id)
	}

	suspend fun rateAlbum(album: DomainAlbum, rating: Int) {
		val ratedEntity = album.toEntity().copy(
			userRating = rating
		)
		albumDao.insertAlbum(ratedEntity)
		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, album.id)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, album.id)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, album.id)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, album.id)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, album.id)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, album.id)
		}
	}

	fun getAlbumLetterCounts(
		reversed: Boolean = false,
		filters: Set<DomainFilter> = emptySet()
	): Flow<List<LetterCount>> {
		return albumDao.getAlbumLetterCounts(getAlbumLetterCountsQuery(reversed, filters))
	}
}
