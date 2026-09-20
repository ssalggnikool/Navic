package paige.navic.ui.util

// this is just a stub, don't use this on iOS
actual object AddressUtil {
    actual fun isAddressLocal(address: String): Boolean {
        return true
    }

	actual fun extractHostFromUri(uri: String): String? {
		return null
	}
}
