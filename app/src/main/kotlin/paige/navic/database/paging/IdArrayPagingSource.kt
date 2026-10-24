/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

suspend fun <Entity : Any, Domain : Any> createPagedFlow(
	isRandom: Boolean,
	getRandomIds: suspend () -> List<String>,
	getEntityId: (Entity) -> String,
	fetchByIds: suspend (List<String>) -> List<Entity>,
	getStandardQuery: () -> PagingSource<Int, Entity>,
	toDomain: (Entity) -> Domain
): Flow<PagingData<Domain>> {
	val pagingSourceFactory: () -> PagingSource<Int, Entity> = if (isRandom) {
		createRandomPagingSourceFactory(
			getIds = getRandomIds,
			getId = getEntityId,
			fetchByIds = fetchByIds
		)
	} else {
		getStandardQuery
	}

	return Pager(
		config = PagingConfig(pageSize = 20, maxSize = 100),
		pagingSourceFactory = pagingSourceFactory
	).flow.map { pagingData -> pagingData.map { toDomain(it) } }
}

suspend fun <Value : Any> createRandomPagingSourceFactory(
	getIds: suspend () -> List<String>,
	getId: (Value) -> String,
	fetchByIds: suspend (List<String>) -> List<Value>
): () -> PagingSource<Int, Value> {
	val shuffledIds = withContext(Dispatchers.Default) {
		getIds().shuffled()
	}
	return {
		IdArrayPagingSource(
			ids = shuffledIds,
			getId = getId,
			fetchByIds = fetchByIds
		)
	}
}

class IdArrayPagingSource<Value : Any>(
	private val ids: List<String>,
	private val getId: (Value) -> String,
	private val fetchByIds: suspend (List<String>) -> List<Value>
) : PagingSource<Int, Value>() {

	override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
		return state.anchorPosition?.let { anchorPosition ->
			state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
				?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
		}
	}

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
		val start = params.key ?: 0
		val end = minOf(start + params.loadSize, ids.size)
		val pageIds = ids.subList(start.coerceAtMost(end), end)

		return try {
			val order = pageIds.withIndex().associate { (i, id) -> id to i }
			val items = fetchByIds(pageIds).sortedBy { order[getId(it)] ?: Int.MAX_VALUE }

			LoadResult.Page(
				data = items,
				prevKey = if (start > 0) maxOf(0, start - params.loadSize) else null,
				nextKey = if (end < ids.size) end else null
			)
		} catch (e: CancellationException) {
			throw e
		} catch (e: Exception) {
			LoadResult.Error(e)
		}
	}
}
