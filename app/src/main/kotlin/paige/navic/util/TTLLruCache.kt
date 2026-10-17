/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import android.os.SystemClock
import android.util.LruCache

class TTLLruCache<K, V>(
    maxSize: Int, 
    private val defaultTtlMillis: Long = 60_000L
) {
	private data class CacheEntry<V>(
        val value: V,
        val expiryTimeMillis: Long
    )

	private val lruCache = LruCache<K, CacheEntry<V>>(maxSize)

    fun get(key: K): V? {
        val entry = lruCache.get(key) ?: return null

        if (SystemClock.elapsedRealtime() > entry.expiryTimeMillis) {
            lruCache.remove(key)
            return null
        }

        return entry.value
    }

    fun put(key: K, value: V, ttlMillis: Long = defaultTtlMillis) {
        val expiryTime = SystemClock.elapsedRealtime() + ttlMillis
        lruCache.put(key, CacheEntry(value, expiryTime))
    }

    fun remove(key: K): V? {
        return lruCache.remove(key)?.value
    }

    fun clear() {
        lruCache.evictAll()
    }

    fun size(): Int {
        return lruCache.size()
    }
}
