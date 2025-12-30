package es.joshluq.encryptionkit.data

import es.joshluq.encryptionkit.domain.CryptoEngine
import es.joshluq.encryptionkit.domain.CryptoException
import es.joshluq.encryptionkit.domain.CryptoResult
import es.joshluq.encryptionkit.domain.EncryptionkitConfigProvider
import es.joshluq.encryptionkit.domain.KeyManager
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [CryptoEngine] using AES-GCM.
 */
@Singleton
class AesCryptoEngine @Inject constructor(
    private val keyManager: KeyManager,
    private val configProvider: EncryptionkitConfigProvider
) : CryptoEngine {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
    }

    override fun encrypt(data: ByteArray): CryptoResult {
        try {
            val key = getKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val ciphertext = cipher.doFinal(data)
            val iv = cipher.iv

            return CryptoResult(ciphertext, iv)
        } catch (e: Exception) {
            throw mapToCryptoException(e, CryptoException.ErrorType.ENCRYPTION_FAILED)
        }
    }

    override fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        try {
            val key = getKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw mapToCryptoException(e, CryptoException.ErrorType.DECRYPTION_FAILED)
        }
    }

    private fun getKey(): Key {
        val alias = configProvider.config.alias
        return keyManager.getKey(alias)
            ?: throw CryptoException(
                "Key not found: $alias",
                null,
                CryptoException.ErrorType.KEY_NOT_FOUND
            )
    }

    private fun mapToCryptoException(e: Exception, defaultType: CryptoException.ErrorType): CryptoException {
        if (e is CryptoException) return e
        
        // Check for User Authentication requirement failure
        if (e is android.security.keystore.UserNotAuthenticatedException) {
            return CryptoException(
                "User not authenticated",
                e,
                CryptoException.ErrorType.USER_NOT_AUTHENTICATED
            )
        }

        return CryptoException(e.message ?: "Crypto operation failed", e, defaultType)
    }
}
