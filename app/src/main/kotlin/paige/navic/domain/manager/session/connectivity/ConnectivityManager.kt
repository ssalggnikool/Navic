/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session.connectivity

import android.content.Context
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import android.net.ConnectivityManager as AndroidConnectivityManager


interface NetworkStatus {
	data class LinkPropertiesChanged(
		val linkProperties: LinkProperties
	) : NetworkStatus

	data class NetworkCapsChanged(
		val caps: NetworkCapabilities
	) : NetworkStatus

	data class NetworkAvailable(
		val network: Network
	) : NetworkStatus

	data class NetworkLost(
		val network: Network
	) : NetworkStatus
}

fun NetworkCapabilities.isMetered() = !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

class ConnectivityManager(
	context: Context,
) {
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as AndroidConnectivityManager

	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	val status = callbackFlow {
		val callback = object : AndroidConnectivityManager.NetworkCallback() {
			override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
				super.onLinkPropertiesChanged(network, linkProperties)
				trySend(
					NetworkStatus.LinkPropertiesChanged(linkProperties)
				)
			}

			override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
				super.onCapabilitiesChanged(network, caps)
				trySend(
					NetworkStatus.NetworkCapsChanged(caps)
				)
			}

			override fun onAvailable(network: Network) {
				super.onAvailable(network)
				trySend(NetworkStatus.NetworkAvailable(network))
			}

			override fun onLost(network: Network) {
				super.onLost(network)
				trySend(NetworkStatus.NetworkLost(network))
			}
		}

		connectivityManager.registerDefaultNetworkCallback(callback)
		awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
	}.conflate()

	val isMetered = status
		.map { (it as? NetworkStatus.NetworkCapsChanged)?.caps?.isMetered() ?: false }
		.distinctUntilChanged()
		.stateIn(scope, SharingStarted.Eagerly, false)

}
