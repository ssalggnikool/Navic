package paige.navic.util

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

expect fun getDefaultEngineForPlatform(): HttpClientEngine?

expect fun HttpClientConfig<*>.configureSsl(trustAllCerts: Boolean = false)
