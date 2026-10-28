/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session.connectivity

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.manager.session.SessionManager
import paige.navic.domain.manager.session.SessionState
import paige.navic.domain.model.settings.OfflineMode
import kotlin.time.Duration.Companion.milliseconds

sealed interface ConnectivityState {
	data object Offline : ConnectivityState
	data object Checking : ConnectivityState
	data object Online : ConnectivityState
	data class ServerUnreachable(val cause: Throwable?) : ConnectivityState
}

class ReachabilityFlow(
	private val network: ConnectivityManager,
	private val preferenceManager: PreferenceManager,
	private val sessionManager: SessionManager
) {
	private sealed interface Trigger {
		data class NetworkEvent(val event: NetworkStatus) : Trigger
		data object SessionReady : Trigger
		data object OfflineModeChanged : Trigger
	}

	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	suspend fun ping(): Result<Unit> = withContext(Dispatchers.IO) {
		runCatching {
			check(sessionManager.instanceUrl.isNotBlank()) { "No server configured" }
			sessionManager.api.ping()
		}.map { }
	}

	private val triggers: Flow<Trigger> = merge(
		network.status.map { Trigger.NetworkEvent(it) },
		sessionManager.sessionState.filterIsInstance<SessionState.Authenticated>().map { status ->
			if (status.currentUser != null) Trigger.SessionReady else Trigger.OfflineModeChanged
		},
		snapshotFlow { preferenceManager.offlineMode }.map { Trigger.OfflineModeChanged }
	)

	@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
	val state: StateFlow<ConnectivityState> = triggers
		.debounce(300.milliseconds)
		.transformLatest { trigger ->
			val mode = preferenceManager.offlineMode
			val offline = mode == OfflineMode.Forced ||
				(mode == OfflineMode.NoWiFi && network.isMetered.value) ||
				(trigger is Trigger.NetworkEvent && trigger.event is NetworkStatus.NetworkLost)

			if (offline) {
				emit(ConnectivityState.Offline)
			} else {
				emit(ConnectivityState.Checking)
				emit(
					ping().fold(
						onSuccess = { ConnectivityState.Online },
						onFailure = { ConnectivityState.ServerUnreachable(it) },
					)
				)
			}
		}
		.distinctUntilChanged()
		.stateIn(scope, SharingStarted.Eagerly, ConnectivityState.Checking)

	val isMetered: StateFlow<Boolean> = network.isMetered

	val isReachable: StateFlow<Boolean> = state
		.map { it is ConnectivityState.Online || it is ConnectivityState.Checking }
		.distinctUntilChanged()
		.stateIn(scope, SharingStarted.Eagerly, true)
}
