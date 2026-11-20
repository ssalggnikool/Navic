/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import paige.navic.domain.manager.session.AccountStore
import paige.navic.ui.core.PlayerUiState
import paige.navic.util.Logger

class PlayerStateRepository(
	private val preferences: DataStore<Preferences>,
	accountStore: AccountStore
) {
	// scoped per account, legacy accounts keep the unprefixed key
	private val key = stringPreferencesKey("${accountStore.keyPrefix}player_state")

	private val json = Json {
		ignoreUnknownKeys = true
	}

	suspend fun getState(): PlayerUiState {
		val encoded = preferences.data.first()[key] ?: return PlayerUiState()
		return try {
			json.decodeFromString<PlayerUiState>(encoded)
		} catch (ex: SerializationException) {
			Logger.e("PlayerStateRepository", "failed to deserialise state", ex)
			PlayerUiState()
		} catch (ex: Exception) {
			Logger.e("PlayerStateRepository", "failed to read state", ex)
			PlayerUiState()
		}
	}

	suspend fun setState(value: PlayerUiState) {
		try {
			preferences.edit { it[key] = json.encodeToString(value) }
		} catch (ex: SerializationException) {
			Logger.e("PlayerStateRepository", "failed to serialise state", ex)
		} catch (ex: Exception) {
			Logger.e("PlayerStateRepository", "failed to save state", ex)
		}
	}
}
