	/*
	 * Navic, a Subsonic music streaming app for Android
	 * Copyright (c) 2026 paige
	 * SPDX-License-Identifier: GPL-3.0-only
	 */

	package paige.navic.util

	import android.security.keystore.KeyGenParameterSpec
	import android.security.keystore.KeyProperties
	import com.russhwolf.settings.Settings
	import java.security.KeyPair
	import java.security.KeyPairGenerator
	import java.security.KeyStore
	import java.security.spec.MGF1ParameterSpec
	import javax.crypto.Cipher
	import javax.crypto.spec.OAEPParameterSpec
	import javax.crypto.spec.PSource

	object CredentialEncryptor {
		private const val ANDROID_KEY_STORE = "AndroidKeyStore"
		private const val RSA_ALIAS = "navic_credentials_rsa_v1"
		private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
		private const val PREFIX = "enc:v1:"
		private val OAEP_SPEC = OAEPParameterSpec(
			"SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT
		)

		private fun getOrCreateKeyPair(): KeyPair {
			val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

			(keyStore.getEntry(RSA_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.let {
				return KeyPair(it.certificate.publicKey, it.privateKey)
			}

			val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
			val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
				RSA_ALIAS,
				KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
			).run {
				setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
				setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
				build()
			}

			kpg.initialize(parameterSpec)
			return kpg.generateKeyPair()
		}

		fun encrypt(plainText: String): String {
			if (plainText.isEmpty()) return ""
			return try {
				val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
				cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeyPair().public, OAEP_SPEC)

				val bytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
				PREFIX + bytes.toHexString()
			} catch (e: Exception) {
				Logger.e("CredentialEncryptor", "Failed to encrypt credential", e)
				throw IllegalStateException("Credential encryption failed", e)
			}
		}

		fun decrypt(cipherText: String): String {
			if (cipherText.isEmpty()) return ""
			if (!cipherText.startsWith(PREFIX)) return cipherText

			return try {
				val bytes = cipherText.removePrefix(PREFIX).hexToByteArray()
				val cipher = Cipher.getInstance(RSA_TRANSFORMATION)

				cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeyPair().private, OAEP_SPEC)
				String(cipher.doFinal(bytes), Charsets.UTF_8)
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
