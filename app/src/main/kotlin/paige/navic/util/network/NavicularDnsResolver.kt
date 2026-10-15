/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util.network

import com.russhwolf.settings.Settings
import okhttp3.Dns
import okhttp3.Interceptor
import paige.navic.domain.manager.PreferenceManager
import java.net.InetAddress

class NavicularDnsResolver(
	val preferenceManager: PreferenceManager,
	val settings: Settings
) : Dns {
	override fun lookup(hostname: String): List<InetAddress> {
		val serverDomain = settings.getStringOrNull("instanceUrl") ?: error("instanceUrl is null")
		val systemAddresses = Dns.SYSTEM.lookup(hostname)
		if (hostname == serverDomain) {
			val alternativeAddresses = preferenceManager.alternateServerAddresses
				.map { InetAddress.getByName(it) }

			return (systemAddresses + alternativeAddresses).sortedByDescending { it.isLocalOrPrivate() }
		}
		return systemAddresses
	}

	fun rewriteRequestInterceptor() = Interceptor { chain ->
		var request = chain.request()
		val host = request.url.host

		val isLocal = try {
			val addresses = this.lookup(host)
			addresses.any { it.isLocalOrPrivate() }
		} catch (_: Exception) {
			false
		}

		if (isLocal && request.isHttps) {
			val newUrl = request.url.newBuilder()
				.scheme("http")
				.build()

			request = request.newBuilder()
				.url(newUrl)
				.build()
		}

		try {
			chain.proceed(request)
		} catch (e: Exception) {
			if (!isLocal && request.isHttps) {
				val fallbackUrl = request.url.newBuilder()
					.scheme("http")
					.build()
				val fallbackRequest = request.newBuilder()
					.url(fallbackUrl)
					.build()

				chain.proceed(fallbackRequest)
			} else {
				throw e
			}
		}
	}
}

fun InetAddress.isLocalOrPrivate(): Boolean {
	return this.isLoopbackAddress ||
		this.isSiteLocalAddress ||
		this.isLinkLocalAddress ||
		this.isAnyLocalAddress
}
