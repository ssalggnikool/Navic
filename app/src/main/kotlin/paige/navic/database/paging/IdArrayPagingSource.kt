/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.database.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.coroutines.cancellation.CancellationException

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
