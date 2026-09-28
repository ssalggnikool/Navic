package paige.navic.domain.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import paige.navic.domain.models.settings.EqualizerConfig
import paige.navic.util.Logger

class EqualizerManager(
	private val preferences: DataStore<Preferences>
) {
	private val json = Json
	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val config = preferenceStateFlow {
		return@preferenceStateFlow try {
			json.decodeFromString<EqualizerConfig>(
				it[KEY_CONFIG] ?: return@preferenceStateFlow EqualizerConfig()
			)
		} catch(ex: SerializationException) {
			Logger.e("EqualizerManager", "failed to deserialise config", ex)
			EqualizerConfig()
		} catch(ex: Exception) {
			Logger.e("EqualizerManager", "failed to read config", ex)
			EqualizerConfig()
		}
	}

	suspend fun setConfig(value: EqualizerConfig) {
		try {
			preferences.edit { it[KEY_CONFIG] = json.encodeToString(value) }
		} catch (ex: SerializationException) {
			Logger.e("EqualizerManager", "failed to serialise config", ex)
		} catch (ex: Exception) {
			Logger.e("EqualizerManager", "failed to save config", ex)
		}
	}

	private inline fun <T> preferenceStateFlow(crossinline transform: (Preferences) -> T): StateFlow<T> {
		return preferences.data
			.map(transform)
			.stateIn(
				scope = scope,
				started = SharingStarted.Eagerly,
				initialValue = transform(emptyPreferences())
			)
	}

	private companion object {
		val KEY_CONFIG = stringPreferencesKey("equalizer_config")
	}
}
