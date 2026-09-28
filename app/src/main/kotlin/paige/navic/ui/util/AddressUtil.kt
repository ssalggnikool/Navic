package paige.navic.ui.util

import androidx.core.net.toUri
import java.net.InetAddress

object AddressUtil {
	fun isAddressLocal(address: String): Boolean {
		return InetAddress.getByName(address).isLinkLocalAddress
	}

	fun extractHostFromUri(uri: String): String? {
		return uri.toUri().host
	}
}
