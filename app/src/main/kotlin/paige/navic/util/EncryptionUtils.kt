/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.russhwolf.settings.Settings
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CredentialEncryptor {
	private const val ANDROID_KEY_STORE = "AndroidKeyStore"
	private const val KEY_ALIAS = "navic_credentials_key_v1"
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val PREFIX = "enc:v1:"

	@Synchronized
	private fun getOrCreateSecretKey(): SecretKey {
		val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
		(keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

		val keyGenerator =
			KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
		keyGenerator.init(
			KeyGenParameterSpec.Builder(
				KEY_ALIAS,
				KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
			)
				.setKeySize(256)
				.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
				.build()
		)
		return keyGenerator.generateKey()
	}

	fun encrypt(plainText: String): String {
		if (plainText.isEmpty()) return ""
		return try {
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

			val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
			val combined = cipher.iv + encrypted

			PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
		} catch (e: Exception) {
			Logger.e("CredentialEncryptor", "Failed to encrypt credential", e)
			throw IllegalStateException("Credential encryption failed", e)
		}
	}

	fun decrypt(cipherText: String): String {
		if (cipherText.isEmpty()) return ""
		if (!cipherText.startsWith(PREFIX)) return cipherText

		return try {
			val combined = Base64.decode(cipherText.removePrefix(PREFIX), Base64.NO_WRAP)
			val iv = combined.copyOfRange(0, 12)
			val encrypted = combined.copyOfRange(12, combined.size)
			val cipher = Cipher.getInstance(TRANSFORMATION)

			cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
			String(cipher.doFinal(encrypted), Charsets.UTF_8)
		} catch (e: Exception) {
			Logger.e("CredentialEncryptor", "Failed to decrypt credential", e)
			""
		}
	}

	fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)
}

fun Settings.getEncryptedString(key: String, defaultValue: String = ""): String {
	val raw = getString(key, "")
	if (raw.isEmpty()) return defaultValue
	if (!CredentialEncryptor.isEncrypted(raw)) {
		insertEncryptedString(key, raw)
		return raw
	}
	val decrypted = CredentialEncryptor.decrypt(raw)
	return decrypted.ifEmpty { defaultValue }
}

fun Settings.getEncryptedString(key: String): String? {
	val value = this@getEncryptedString.getEncryptedString(key, "")
	return value.ifEmpty { null }
}

fun Settings.insertEncryptedString(key: String, value: String?) {
	if (value == null) {
		remove(key)
	} else {
		putString(key, CredentialEncryptor.encrypt(value))
	}
}
