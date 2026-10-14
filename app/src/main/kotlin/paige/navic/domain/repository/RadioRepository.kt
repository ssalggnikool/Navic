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
import paige.navic.database.dao.RadioDao
import paige.navic.database.mapper.toDomainModel
import paige.navic.domain.model.DomainRadio

class RadioRepository(
	private val radioDao: RadioDao,
	private val dbRepository: DbRepository
) {
	private fun getLocalData(): Flow<PagingData<DomainRadio>> {
		return Pager(
			config = PagingConfig(
				pageSize = 20,
				maxSize = 100
			),
			pagingSourceFactory = { radioDao.getRadios() }
		).flow.map { pagingData -> pagingData.map { it.toDomainModel() } }
	}

	private suspend fun refreshLocalData(): Flow<PagingData<DomainRadio>> {
		dbRepository.syncRadios().getOrThrow()
		return getLocalData()
	}

	suspend fun getRadios(
		fullRefresh: Boolean
	): Flow<PagingData<DomainRadio>> {
		val localData = getLocalData()
		return if (fullRefresh) {
			refreshLocalData()
		} else {
			localData
		}
	}
}
