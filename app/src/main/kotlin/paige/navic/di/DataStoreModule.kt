/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// eventually kmp settings should probably be replaced with this
fun createDataStore(producePath: () -> Path): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(produceFile = producePath)
}

const val DATA_STORE_FILE_NAME = "preferences.preferences_pb"

val dataStoreModule = module {
	single<DataStore<Preferences>> {
		createDataStore(
			producePath = {
				androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).toOkioPath()
			}
		)
	}
}
