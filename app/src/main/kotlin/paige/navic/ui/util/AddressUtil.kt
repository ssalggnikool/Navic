/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

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
