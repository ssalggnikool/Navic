package paige.navic.util

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * no-op trust manager. meant to be used if the user/developer explicitly wants to trust ALL certificates
 * no matter if they're from the user or the system store
 */
internal class NoopTrustManager : X509TrustManager {

	@Suppress("TrustAllX509TrustManager")
	override fun checkServerTrusted(
		chain: Array<X509Certificate>,
		authType: String
	) {
		// no-op
	}

	@Suppress("TrustAllX509TrustManager")
	override fun checkClientTrusted(
		chain: Array<X509Certificate>,
		authType: String
	) {
		// no-op
	}

	override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}
