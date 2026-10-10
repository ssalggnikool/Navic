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
import paige.navic.data.database.dao.RadioDao
import paige.navic.data.database.mapper.toDomainModel
import paige.navic.domain.model.DomainRadio
import paige.navic.ui.core.UiState

class RadioRepository(
	private val radioDao: RadioDao,
	private val dbRepository: DbRepository
) {
	private suspend fun getLocalData(): ImmutableList<DomainRadio> {
		return radioDao
			.getRadios()
			.map { it.toDomainModel() }
			.toImmutableList()
	}

	private suspend fun refreshLocalData(): ImmutableList<DomainRadio> {
		dbRepository.syncRadios().getOrThrow()
		return getLocalData()
	}

	suspend fun getRadios(
		fullRefresh: Boolean
	): ImmutableList<DomainRadio> {
		val localData = getLocalData()
		return if (fullRefresh) {
			refreshLocalData()
		} else {
			localData
		}
	}
}
