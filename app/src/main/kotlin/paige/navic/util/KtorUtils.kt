/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.http
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import okhttp3.OkHttpClient
import paige.navic.domain.manager.PreferenceManager
import java.net.Proxy
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

val PROXY_URL_REGEX = Regex("(socks[4-5]?|https?)?://(.+):?(\\d+)")

fun createSSLSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory? {
	return SSLContext.getInstance("SSL").also {
		it.init(null, trustManagers, SecureRandom())
	}.socketFactory
}

fun buildKtorEngine(proxyUrl: String? = null, trustAllCerts: Boolean = false): HttpClientEngine {
	return OkHttp.create {
		config {
			if (trustAllCerts) {
				val noopTrustManager = NoopTrustManager()
				createSSLSocketFactory(
					arrayOf(noopTrustManager)
				)?.let {
					sslSocketFactory(it, noopTrustManager)
				}
			}

			proxyUrl?.let {
				proxy(extractProxyConfigFromUrl(it))
			}
		}
	}
}

// media3's ktor data source is bugged
fun createOkHttpClient(
	trustAllCerts: Boolean,
	customHeaders: Map<String, String>?,
	proxyUrl: String?
): OkHttpClient {
	return OkHttpClient.Builder().apply {
		proxyUrl?.let {
			proxy(extractProxyConfigFromUrl(it))
		}

		if (trustAllCerts) {
			val noopTrustManager = NoopTrustManager()
			createSSLSocketFactory(
				arrayOf(noopTrustManager)
			)?.let {
				sslSocketFactory(it, noopTrustManager)
			}
		}

		if (!customHeaders.isNullOrEmpty()) {
			addInterceptor {
				val request = it.request()
					.newBuilder()
					.apply {
						customHeaders.forEach(::header)
					}.build()

				it.proceed(request)
			}
		}
	}.build()
}

fun createHttpClient(
	trustAllCerts: Boolean = false,
	customHeaders: Map<String, String>? = null,
	proxyUrl: String? = null
): HttpClient {
	val engine = buildKtorEngine(
		proxyUrl,
		trustAllCerts
	)
	val block: HttpClientConfig<*>.() -> Unit = {
		if (customHeaders?.isNotEmpty() == true) {
			defaultRequest {
				customHeaders.forEach { (key, value) -> header(key, value) }
			}
		}
	}

	return HttpClient(engine, block)
}

fun extractProxyConfigFromUrl(url: String): ProxyConfig? {
	if (url.startsWith("http")) {
		return ProxyBuilder.http(url)
	} else if (url.startsWith("socks")) {
		val match = PROXY_URL_REGEX.matchEntire(url)

		if (match?.groupValues?.isNotEmpty() == true) {
			val port = match.groupValues.getOrNull(1)?.toIntOrNull()

			return ProxyBuilder.socks(
				match.groupValues[0],
				port ?: 1080
			)
		}
	}
	return null
}

fun createHttpClientWithPreferences(preferenceManager: PreferenceManager): HttpClient {
	return createHttpClient(
		preferenceManager.dangerousSslNoopEnabled,
		preferenceManager.customHeadersMap(),
		preferenceManager.proxyUrl
	)
}
