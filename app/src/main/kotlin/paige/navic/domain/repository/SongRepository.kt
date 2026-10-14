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
import paige.navic.database.dao.SongDao
import paige.navic.database.entity.SyncActionType
import paige.navic.database.mapper.toDomainModel
import paige.navic.database.mapper.toEntity
import paige.navic.database.query.toSqlQuery
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.model.DomainFilter
import paige.navic.domain.model.DomainSong
import paige.navic.domain.model.DomainSongListType
import kotlin.time.Clock

class SongRepository(
	private val songDao: SongDao,
	private val dbRepository: DbRepository,
	private val syncManager: SyncManager
) {
	suspend fun getSongById(id: String) = songDao.getSongById(id)?.toDomainModel()

	fun getAllSongs(): Flow<PagingData<DomainSong>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				songDao.getAllPagedSongs()
			}
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	fun getRandomSongs(count: Int): Flow<PagingData<DomainSong>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				songDao.getRandomSongs(count)
			}
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	suspend fun getRandomSongsList(count: Int): List<DomainSong> {
		return songDao.getRandomSongsList(count).map { it.toDomainModel() }
	}

	private fun getLocalData(
		listType: DomainSongListType,
		reversed: Boolean = false,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainSong>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				songDao.getPagedSongsByQuery(listType.toSqlQuery(reversed, filters))
			}
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun refreshLocalData(
		listType: DomainSongListType,
		reversed: Boolean = false,
		filters: Set<DomainFilter> = emptySet()
	): Flow<PagingData<DomainSong>> {
		dbRepository.syncLibrarySongs().getOrThrow()
		return getLocalData(listType, reversed, filters)
	}

	suspend fun getSongs(
		fullRefresh: Boolean,
		listType: DomainSongListType,
		reversed: Boolean,
		filters: Set<DomainFilter>
	): Flow<PagingData<DomainSong>> {
		val localData = getLocalData(listType, reversed, filters)
		return if (fullRefresh) {
			refreshLocalData(listType, reversed, filters)
		} else {
			localData
		}
	}

	suspend fun isSongStarred(song: DomainSong) = songDao.isSongStarred(song.id)
	suspend fun getSongRating(song: DomainSong) = songDao.getSongRating(song.id) ?: 0
	suspend fun starSong(song: DomainSong) {
		val starredEntity = song.toEntity().copy(
			starredAt = Clock.System.now()
		)
		songDao.insertSong(starredEntity)
		syncManager.enqueueAction(SyncActionType.STAR, song.id)
	}

	suspend fun unstarSong(song: DomainSong) {
		val unstarredEntity = song.toEntity().copy(
			starredAt = null
		)
		songDao.insertSong(unstarredEntity)
		syncManager.enqueueAction(SyncActionType.UNSTAR, song.id)
	}

	suspend fun rateSong(song: DomainSong, rating: Int) {
		val ratedEntity = song.toEntity().copy(
			userRating = rating
		)
		songDao.insertSong(ratedEntity)
		when (rating) {
			0 -> syncManager.enqueueAction(SyncActionType.STAR_0, song.id)
			1 -> syncManager.enqueueAction(SyncActionType.STAR_1, song.id)
			2 -> syncManager.enqueueAction(SyncActionType.STAR_2, song.id)
			3 -> syncManager.enqueueAction(SyncActionType.STAR_3, song.id)
			4 -> syncManager.enqueueAction(SyncActionType.STAR_4, song.id)
			5 -> syncManager.enqueueAction(SyncActionType.STAR_5, song.id)
		}
	}
}
