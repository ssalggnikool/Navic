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
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.data.database.query.toGenreSqlQuery
import paige.navic.domain.model.DomainGenre

class GenreRepository(
	private val genreDao: GenreDao,
	private val dbRepository: DbRepository
) {
	private fun getLocalData(): Flow<PagingData<DomainGenre>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = {
				genreDao.getGenresByQuery(toGenreSqlQuery())
			}
		).flow.map { data -> data.map { it.toDomainModel() } }
	}

	private suspend fun refreshLocalData(): Flow<PagingData<DomainGenre>> {
		dbRepository.syncGenres().getOrThrow()
		return getLocalData()
	}

	suspend fun getGenres(
		fullRefresh: Boolean
	): Flow<PagingData<DomainGenre>> {
		val localData = getLocalData()
		return if (fullRefresh) {
			refreshLocalData()
		} else {
			localData
		}
	}
}
