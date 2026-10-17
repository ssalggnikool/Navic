/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.http
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import okhttp3.Dns
import okhttp3.OkHttpClient
import paige.navic.domain.manager.PreferenceManager
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

val PROXY_URL_REGEX = Regex("(socks[4-5]?|https?)?://(.+):?(\\d+)")

fun createSSLSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory? {
	return SSLContext.getInstance("SSL").also {
		it.init(null, trustManagers, SecureRandom())
	}.socketFactory
}

fun buildKtorEngine(proxyUrl: String? = null, trustAllCerts: Boolean = false, dnsResolver: Dns? = null): HttpClientEngine {
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

			dnsResolver?.let {
				dns(it)
				if (it is NavicularDnsResolver) {
					this@create.addInterceptor(it.requestInterceptor)
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
	proxyUrl: String?,
	dnsResolver: Dns? = null
): OkHttpClient {
	return OkHttpClient.Builder().apply {
		proxyUrl?.let {
			proxy(extractProxyConfigFromUrl(it))
		}

		connectTimeout(3, TimeUnit.SECONDS)

		if (trustAllCerts) {
			val noopTrustManager = NoopTrustManager()
			createSSLSocketFactory(
				arrayOf(noopTrustManager)
			)?.let {
				sslSocketFactory(it, noopTrustManager)
			}
		}

		dnsResolver?.let {
			dns(it)
			if (it is NavicularDnsResolver) {
				this.addInterceptor(it.requestInterceptor)
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
	proxyUrl: String? = null,
	dnsResolver: Dns? = null
): HttpClient {
	val engine = buildKtorEngine(
		proxyUrl,
		trustAllCerts,
		dnsResolver
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

fun createHttpClientWithPreferences(preferenceManager: PreferenceManager, dnsResolver: NavicularDnsResolver): HttpClient {
	return createHttpClient(
		preferenceManager.dangerousSslNoopEnabled,
		preferenceManager.customHeadersMap(),
		preferenceManager.proxyUrl,
		dnsResolver
	)
}
