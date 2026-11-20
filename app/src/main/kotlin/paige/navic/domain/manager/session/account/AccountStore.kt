/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager.session.account

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import paige.navic.util.Logger
import paige.navic.util.getEncryptedString
import paige.navic.util.insertEncryptedString

class AccountStore(
	private val settings: Settings
) {
	private companion object {
		const val TAG = "AccountStore"
		const val ACCOUNTS = "accounts"
		const val ACTIVE_ACCOUNT = "activeAccountId"
		const val PENDING_DELETIONS = "pendingDeletedAccounts"
	}

	private val json = Json {
		ignoreUnknownKeys = true
		encodeDefaults = true
	}

	private val _accounts = MutableStateFlow<List<Account>>(emptyList())
	val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

	val activeAccountId: String?
		get() = settings.getString(ACTIVE_ACCOUNT, "").ifBlank { null }

	val activeAccount: Account?
		get() = account(activeAccountId)

	val keyPrefix: String
		get() = keyPrefixFor(activeAccountId)

	init {
		_accounts.value = readAccounts()
	}

	fun account(id: String?): Account? =
		_accounts.value.firstOrNull { it.id == id }

	fun keyPrefixFor(id: String?): String =
		if (id == null) "" else "$id/"

	fun upsert(account: Account): Account {
		val existing = _accounts.value.firstOrNull {
			it.baseUrl == account.baseUrl &&
				it.authMethod == account.authMethod &&
				it.username == account.username
		}
		val stored = account.copy(id = existing?.id ?: account.id)
		val updated = if (existing == null) {
			_accounts.value + stored
		} else {
			_accounts.value.map { if (it.id == stored.id) stored else it }
		}
		writeAccounts(updated)
		return stored
	}

	fun setActive(id: String?) {
		if (id == null) {
			settings.remove(ACTIVE_ACCOUNT)
		} else {
			settings[ACTIVE_ACCOUNT] = id
		}
	}

	fun updateBaseUrl(id: String, baseUrl: String) {
		writeAccounts(_accounts.value.map {
			if (it.id == id) it.copy(baseUrl = baseUrl) else it
		})
	}

	fun remove(id: String) {
		writeAccounts(_accounts.value.filterNot { it.id == id })
		if (activeAccountId == id) {
			setActive(_accounts.value.firstOrNull()?.id)
		}
	}

	fun queueDatabaseDeletion(id: String) {
		val pending = (pendingDeletions() + id).distinct()
		settings[PENDING_DELETIONS] = pending.joinToString(",")
	}

	fun claimDatabaseDeletions(): List<String> {
		val pending = pendingDeletions()
		settings.remove(PENDING_DELETIONS)
		return pending
	}

	private fun pendingDeletions(): List<String> =
		settings.getString(PENDING_DELETIONS, "")
			.split(",")
			.filter { it.isNotBlank() }

	private fun readAccounts(): List<Account> {
		val raw = settings.getEncryptedString(ACCOUNTS, "")
		if (raw.isBlank()) return emptyList()

		return try {
			json.decodeFromString<List<Account>>(raw)
		} catch (e: Exception) {
			Logger.e(TAG, "failed to read accounts", e)
			emptyList()
		}
	}

	private fun writeAccounts(accounts: List<Account>) {
		settings.insertEncryptedString(ACCOUNTS, json.encodeToString(accounts))
		_accounts.value = accounts
	}

}
