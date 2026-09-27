package paige.navic.util

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.security.SecureRandom
import javax.net.ssl.SSLContext


actual fun getDefaultEngineForPlatform(trustAllCerts: Boolean): HttpClientEngine? {
	return OkHttp.create {
		if (trustAllCerts) {
			config {
				val noopTrustManager = NoopTrustManager()

				SSLContext.getInstance("SSL").also {
					it.init(null, arrayOf(noopTrustManager), SecureRandom())
				}.socketFactory.let {
					sslSocketFactory(it, noopTrustManager)
				}
			}
		}
	}
}
