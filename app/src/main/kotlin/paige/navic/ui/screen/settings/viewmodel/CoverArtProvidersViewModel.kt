/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.screen.settings.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.CoverArtHost
import paige.navic.domain.model.settings.CoverArtProvidersConfig

class CoverArtProvidersViewModel(
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val state = MutableStateFlow(preferenceManager.discordCoverArtProviders)

	private fun update(block: (CoverArtProvidersConfig) -> CoverArtProvidersConfig) {
		val next = block(state.value)
		state.value = next
		preferenceManager.discordCoverArtProviders = next
	}

	fun move(from: Int, to: Int) = update { config ->
		config.copy(
			providers = config.providers.toMutableList().apply {
				add(to, removeAt(from))
			}
		)
	}

	fun toggleEnabled(host: CoverArtHost) = update { config ->
		config.copy(
			providers = config.providers.map {
				if (it.host == host) it.copy(enabled = !it.enabled) else it
			}
		)
	}
}
