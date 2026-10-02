package es.joshluq.encryptionkit.data.provider.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import es.joshluq.encryptionkit.di.d
import es.joshluq.encryptionkit.di.e
import es.joshluq.encryptionkit.di.i
import es.joshluq.encryptionkit.domain.model.BiometricCryptoResult
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.foundationkit.log.LoggerKit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Helper class providing hardware-backed, biometric-authenticated cryptographic operations.
 * Keys are generated inside the Android KeyStore with user authentication requirements
 * and locked until biometric authentication (fingerprint, face) succeeds.
 */
class BiometricCryptoHelper internal constructor(
    private val logger: LoggerKit,
) {
    companion object {
        private const val TAG = "BiometricCryptoHelper"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_SIZE = 256
    }

    /**
     * Retrieves an existing biometric secret key from Android KeyStore or generates a new one.
     *
     * @param alias The alias of the key.
     * @param invalidatedByBiometricEnrollment If true, key is permanently invalidated when new biometrics are enrolled.
     */
    fun getOrCreateKey(
        alias: String,
        invalidatedByBiometricEnrollment: Boolean = true,
    ): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        logger.i(TAG) { "Generating new biometric-bound key in Android KeyStore for alias: $alias" }
        return generateBiometricKey(alias, invalidatedByBiometricEnrollment)
    }

    private fun generateBiometricKey(
        alias: String,
        invalidatedByBiometricEnrollment: Boolean,
    ): SecretKey {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val builder =
                KeyGenParameterSpec
                    .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            } else {
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(-1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }

            keyGenerator.init(builder.build())
            return keyGenerator.generateKey()
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to generate biometric key for alias: $alias" }
            throw CryptoException("Failed to generate biometric key for alias: $alias", e, CryptoException.Reason.KEY_GENERATION_FAILED)
        }
    }

    /**
     * Creates an initialized [BiometricPrompt.CryptoObject] for encryption.
     * Pass this to `BiometricPrompt.authenticate(promptInfo, cryptoObject)`.
     */
    fun createEncryptCryptoObject(alias: String): BiometricPrompt.CryptoObject {
        logger.d(TAG) { "Creating encryption CryptoObject for alias: $alias" }
        try {
            val key = getOrCreateKey(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to create encrypt CryptoObject for alias: $alias" }
            throw CryptoException("Failed to create encrypt CryptoObject", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    /**
     * Creates an initialized [BiometricPrompt.CryptoObject] for decryption using the provided [iv].
     * Pass this to `BiometricPrompt.authenticate(promptInfo, cryptoObject)`.
     */
    fun createDecryptCryptoObject(
        alias: String,
        iv: ByteArray,
    ): BiometricPrompt.CryptoObject {
        logger.d(TAG) { "Creating decryption CryptoObject for alias: $alias" }
        try {
            val key = getOrCreateKey(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            return BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to create decrypt CryptoObject for alias: $alias" }
            throw CryptoException("Failed to create decrypt CryptoObject", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    /**
     * Encrypts the provided [secureData] using the authenticated [cryptoObject].
     * Call this inside `onAuthenticationSucceeded(result)`.
     * Zero-trust memory clearing of [secureData] is performed automatically.
     */
    fun encrypt(
        cryptoObject: BiometricPrompt.CryptoObject,
        secureData: SecureBytes,
        associatedData: ByteArray = ByteArray(0),
    ): BiometricCryptoResult {
        val cipher =
            cryptoObject.cipher
                ?: throw CryptoException(
                    "CryptoObject does not contain an initialized Cipher",
                    null,
                    CryptoException.Reason.OPERATION_FAILED,
                )

        return try {
            secureData.use { bytes ->
                if (associatedData.isNotEmpty()) {
                    cipher.updateAAD(associatedData)
                }
                val ciphertext = cipher.doFinal(bytes.data)
                val iv = cipher.iv ?: throw CryptoException("Cipher IV is null", null, CryptoException.Reason.OPERATION_FAILED)
                BiometricCryptoResult(ciphertext, iv)
            }
        } catch (e: Exception) {
            logger.e(TAG, e) { "Biometric encryption failed" }
            throw CryptoException("Biometric encryption failed", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    /**
     * Decrypts the provided [ciphertext] using the authenticated [cryptoObject].
     * Call this inside `onAuthenticationSucceeded(result)`.
     */
    fun decrypt(
        cryptoObject: BiometricPrompt.CryptoObject,
        ciphertext: ByteArray,
        associatedData: ByteArray = ByteArray(0),
    ): SecureBytes {
        val cipher =
            cryptoObject.cipher
                ?: throw CryptoException(
                    "CryptoObject does not contain an initialized Cipher",
                    null,
                    CryptoException.Reason.OPERATION_FAILED,
                )

        return try {
            if (associatedData.isNotEmpty()) {
                cipher.updateAAD(associatedData)
            }
            val plaintext = cipher.doFinal(ciphertext)
            SecureBytes(plaintext)
        } catch (e: Exception) {
            logger.e(TAG, e) { "Biometric decryption failed" }
            throw CryptoException("Biometric decryption failed", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    /**
     * Deletes the biometric key from the Android KeyStore.
     */
    fun deleteKey(alias: String) {
        logger.i(TAG) { "Deleting biometric key for alias: $alias" }
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to delete biometric key for alias: $alias" }
        }
    }
}
