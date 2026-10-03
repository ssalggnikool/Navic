/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.entity.DownloadStatus
import paige.navic.data.database.entity.SyncActionType
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.data.database.mapper.toEntity
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.model.DomainAlbum
import paige.navic.domain.model.DomainAlbumListType
import paige.navic.domain.model.DomainFilter
import paige.navic.ui.core.UiState
import paige.navic.util.toSqlQuery
import kotlin.time.Clock

class AlbumRepository(
	private val albumDao: AlbumDao,
	private val downloadDao: DownloadDao,
	private val syncManager: SyncManager,
	private val dbRepository: DbRepository
) {
	suspend fun getAlbumsPage(
		page: Int,
		pageSize: Int,
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): ImmutableList<DomainAlbum> {
		val downloadedSongIds = if (filters.contains(DomainFilter.Downloaded)) {
			downloadDao.getAllDownloadsList()
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.map { it.songId }
				.toSet()
		} else null

		val query = listType.toSqlQuery(
			limit = pageSize,
			offset = page * pageSize,
			reversed = reversed,
			filters = filters
		)

		return albumDao
			.getAlbumsByQuery(query)
			.map { it.toDomainModel() }
			.filter { album ->
				downloadedSongIds == null || downloadedSongIds.containsAll(album.songs.map { it.id })
			}
			.toImmutableList()
	}

	suspend fun syncLibrary(): Result<Unit> {
		return dbRepository.syncLibrarySongs().map { }
	}

	fun getAlbumsFlow(
		fullRefresh: Boolean,
		listType: DomainAlbumListType,
		reversed: Boolean,
		filters: Set<DomainFilter> = emptySet()
	): Flow<UiState<ImmutableList<DomainAlbum>>> = flow {
		val localData = getAlbumsPage(0, Int.MAX_VALUE, listType, reversed, filters)
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				dbRepository.syncLibrarySongs().getOrThrow()
				emit(UiState.Success(data = getAlbumsPage(0, Int.MAX_VALUE, listType, reversed, filters)))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)

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
}
