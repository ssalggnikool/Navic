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
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.model.DomainGenre
import paige.navic.ui.core.UiState

class GenreRepository(
	private val genreDao: GenreDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(): ImmutableList<DomainGenre> {
		return genreDao
			.getGenresWithAlbums()
			.map { it.toDomainModel() }
			.sortedByDescending { it.albums.count() }
			.filter { it.albums.isNotEmpty() }
			.toImmutableList()
	}

	private suspend fun refreshLocalData(): ImmutableList<DomainGenre> {
		dbRepository.syncGenres().getOrThrow()
		return getLocalData()
	}

	fun getGenresFlow(
		fullRefresh: Boolean
	): Flow<UiState<ImmutableList<DomainGenre>>> = flow {
		val localData = getLocalData()
		if (fullRefresh) {
			emit(UiState.Loading(data = localData))
			try {
				emit(UiState.Success(data = refreshLocalData()))
			} catch (error: Exception) {
				emit(UiState.Error(error = error, data = localData))
			}
		} else {
			emit(UiState.Success(data = localData))
		}
	}.flowOn(Dispatchers.IO)
}
