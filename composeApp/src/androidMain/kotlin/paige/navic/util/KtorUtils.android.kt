package paige.navic.util

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.android.AndroidEngineConfig
import java.security.SecureRandom
import javax.net.ssl.SSLContext

actual fun HttpClientConfig<*>.configureSsl(trustAllCerts: Boolean) {
	engine {
		this as AndroidEngineConfig
		sslManager = { httpsURLConnection ->
			if (trustAllCerts) {
				val noopTrustManager = NoopTrustManager()

				httpsURLConnection.apply {
					sslSocketFactory = SSLContext.getInstance("SSL").also {
						it.init(null, arrayOf(noopTrustManager), SecureRandom())
					}?.socketFactory
				}
			}
		}
	}
}

actual fun getDefaultEngineForPlatform(): HttpClientEngine? {
	return Android.create()
}
