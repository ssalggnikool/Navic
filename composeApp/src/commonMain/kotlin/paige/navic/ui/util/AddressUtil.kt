package paige.navic.ui.util

expect object AddressUtil {
	fun isAddressLocal(address: String): Boolean
	fun extractHostFromUri(uri: String): String?
}
