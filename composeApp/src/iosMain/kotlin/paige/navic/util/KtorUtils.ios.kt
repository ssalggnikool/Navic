package paige.navic.util

import io.ktor.client.engine.HttpClientEngine

actual fun getDefaultEngineForPlatform(trustAllCerts: Boolean): HttpClientEngine? {
    return null
}
