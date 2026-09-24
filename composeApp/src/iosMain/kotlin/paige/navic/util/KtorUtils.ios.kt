package paige.navic.util

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

actual fun getDefaultEngineForPlatform(): HttpClientEngine? {
    return null
}

actual fun HttpClientConfig<*>.configureSsl(trustAllCerts: Boolean) {
	// no-op
}
