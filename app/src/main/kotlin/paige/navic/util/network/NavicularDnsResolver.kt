/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util.network

import com.russhwolf.settings.Settings
import okhttp3.Dns
import paige.navic.domain.manager.PreferenceManager
import java.net.InetAddress

class NavicularDnsResolver(
	val preferenceManager: PreferenceManager,
	val settings: Settings
) : Dns {
	override fun lookup(hostname: String): List<InetAddress> {
		val serverDomain = settings.getStringOrNull("instanceUrl") ?: error("instanceUrl is null")
		val systemAddresses = Dns.SYSTEM.lookup(hostname)
		if (hostname == serverDomain) {
			val alternativeAddresses = preferenceManager.alternateServerAddresses
				.map { InetAddress.getByName(it) }

			return (systemAddresses + alternativeAddresses).sortedBy { it.isLoopbackAddress || it.isAnyLocalAddress }
		}
		return systemAddresses
	}
}
