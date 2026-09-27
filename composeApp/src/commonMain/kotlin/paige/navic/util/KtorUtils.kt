package paige.navic.util

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.http
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import paige.navic.domain.manager.PreferenceManager

val PROXY_URL_REGEX = Regex("(socks[4-5]?|https?)?://(.+):?(\\d+)")

expect fun getDefaultEngineForPlatform(trustAllCerts: Boolean = false): HttpClientEngine?

fun createHttpClient(
	trustAllCerts: Boolean = false,
	customHeaders: Map<String, String>? = null,
	proxyUrl: String? = null
): HttpClient {
	val platformEngine = getDefaultEngineForPlatform(trustAllCerts)
	val block: HttpClientConfig<*>.() -> Unit = {
		if (customHeaders?.isNotEmpty() == true) {
			defaultRequest {
				customHeaders.forEach { (key, value) -> header(key, value) }
			}
		}

		if (!proxyUrl.isNullOrBlank()) {
			engine {
				proxy = extractProxyConfigFromUrl(proxyUrl)
			}
		}
	}

	return if (platformEngine != null) {
		HttpClient(platformEngine, block)
	} else {
		HttpClient(block)
	}
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
