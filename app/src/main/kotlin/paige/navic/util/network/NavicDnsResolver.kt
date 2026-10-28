/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util.network

import androidx.core.net.toUri
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.Interceptor
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.connectivity.ConnectivityManager
import paige.navic.domain.manager.session.connectivity.NetworkStatus
import paige.navic.util.TTLLruCache
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class NavicDnsResolver(
	val preferenceManager: PreferenceManager,
	val settings: Settings,
	val connectivityManager: ConnectivityManager
) : Dns {
	private companion object {
		val lookupCache = TTLLruCache<String, List<InetAddress>>(
			maxSize = 50,
			defaultTtlMillis = TimeUnit.MINUTES.toMillis(5)
		)
		val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	}

	init {
		scope.launch {
			connectivityManager.status
				.filter { it is NetworkStatus.NetworkAvailable || it is NetworkStatus.NetworkLost }
				.collect {
					invalidate()
				}
		}
	}

	/* shorthand for invalidating the lookup cache */
	fun invalidate() = lookupCache.clear()

	override fun lookup(hostname: String): List<InetAddress> {
		val fromCache = lookupCache.get(hostname)
		if (fromCache == null) {
			val toCache = resolve(hostname)
			lookupCache.put(hostname, toCache)
			return toCache
		}
		return fromCache
	}

	fun resolve(hostname: String): List<InetAddress> {
		val serverDomain = settings.getStringOrNull("instanceUrl")?.toUri()?.host
		val systemAddresses = Dns.SYSTEM.lookup(hostname)
		if (hostname != serverDomain) return systemAddresses
		val alternates = hosts.mapNotNull {
			runCatching {
				InetAddress.getByName(it)
			}.getOrNull()
		}

		return (systemAddresses + alternates).sortedByDescending { it.isLocalOrPrivate() }
	}

	private val addresses: List<String>
		get() = preferenceManager.alternateServerAddresses

	private val hosts: List<String>
		get() = addresses.map { it.extractHost() }

	private val hostMap: Map<String, String>
		get() = hosts.zip(addresses).toMap()

	val requestInterceptor: Interceptor
		get() = Interceptor { chain ->
			var request = chain.request()
			val host = request.url.host

			val lookupResult = runBlocking {
				withContext(Dispatchers.IO) {
					try {
						this@NavicDnsResolver.lookup(host)
					} catch (_: Exception) {
						emptyList()
					}
				}
			}

			val localResolution = lookupResult.firstOrNull { it.isLocalOrPrivate() }
			val localHost = hostMap[localResolution?.hostAddress]

			val scheme = localHost?.extractScheme() ?: "http"
			val port = localHost?.extractPort()

			val newUrl = request.url.newBuilder().build(scheme, port)

			if (localResolution != null) {
				request = request.newBuilder().url(newUrl).build()
			}

			chain.proceed(request)
		}

	private fun HttpUrl.Builder.build(scheme: String, port: Int? = null): HttpUrl {
		this.scheme(scheme)
		if (port != null) {
			this.port(port)
		}

		return this.build()
	}

	private fun String.extractScheme(): String? = this.substringBefore("://")
		.lowercase()
		.takeIf { it.isNotBlank() && this != it }

	private fun String.extractHost(): String = this.substringAfter("://").substringBefore(":")

	private fun String.extractPort(): Int? = this.substringAfterLast(":").toIntOrNull()
}

fun InetAddress.isLocalOrPrivate(): Boolean {
	return this.isLoopbackAddress || this.isSiteLocalAddress || this.isLinkLocalAddress
}
