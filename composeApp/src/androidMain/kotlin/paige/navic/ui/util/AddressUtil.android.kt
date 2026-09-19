package paige.navic.ui.util

import androidx.core.net.toUri
import java.net.InetAddress

actual object AddressUtil {
    actual fun isAddressLocal(address: String): Boolean {
		return InetAddress.getByName(address).isLinkLocalAddress
	}

	actual fun extractHostFromUri(uri: String): String? {
		return uri.toUri().host
	}
}
