package es.joshluq.encryptionkit.data.repository

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

internal class EncryptionRepositoryImpl(
    private val keystoreDataSource: KeystoreDataSource,
    private val fileDataSource: FileDataSource
) : EncryptionRepository {

    private val aesTransformation = "AES/GCM/NoPadding"
    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(config: EncryptionConfig) {
        try {
            keystoreDataSource.ensureKeyExists(config)
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override fun encryptSymmetric(data: ByteArray, config: EncryptionConfig): CryptoResult {
        try {
            val key = keystoreDataSource.getKey(config.alias)
                ?: throw CryptoException("Key not found", reason = CryptoException.Reason.KEY_NOT_FOUND)
            
            val cipher = Cipher.getInstance(aesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val ciphertext = cipher.doFinal(data)
            return CryptoResult(ciphertext, cipher.iv)
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, config: EncryptionConfig): ByteArray {
        try {
            val key = keystoreDataSource.getKey(config.alias)
                ?: throw CryptoException("Key not found", reason = CryptoException.Reason.KEY_NOT_FOUND)
            
            val cipher = Cipher.getInstance(aesTransformation)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        return keystoreDataSource.getSecurityLevel(alias)
    }

    override fun deleteKey(alias: String) {
        keystoreDataSource.deleteKey(alias)
    }

    override suspend fun getPublicKey(): PublicKey {
        try {
            return fileDataSource.getPublicKeyFromCertificate()
        } catch (e: CryptoException) {
            throw e
        } catch (e: Exception) {
            throw CryptoException("Failed to load public key", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    override suspend fun encryptAsymmetric(data: ByteArray, config: EncryptionConfig): ByteArray {
        try {
            val publicKey = getPublicKey()
            
            config.publicKeyHash?.let { expectedHash ->
                val currentHash = hash(publicKey.encoded, "SHA-256").joinToString("") { "%02x".format(it) }
                if (!currentHash.equals(expectedHash, ignoreCase = true)) {
                    throw CryptoException(
                        "Public key validation failed. Expected: $expectedHash, Found: $currentHash",
                        reason = CryptoException.Reason.PUBLIC_KEY_PINNING_FAILURE
                    )
                }
            }

            val cipher = Cipher.getInstance(rsaTransformation)
            val oaepParams = OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
            )
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override fun hash(data: ByteArray, algorithm: String): ByteArray {
        return try {
            MessageDigest.getInstance(algorithm).digest(data)
        } catch (e: Exception) {
            throw CryptoException("Hash failed", e, CryptoException.Reason.OPERATION_FAILED)
        }
    }

    private fun mapException(e: Exception): CryptoException {
        if (e is CryptoException) return e

        return when (e) {
            is UserNotAuthenticatedException -> CryptoException(
                "User authentication required", e, CryptoException.Reason.USER_NOT_AUTHENTICATED
            )
            is KeyPermanentlyInvalidatedException -> CryptoException(
                "Key permanently invalidated (biometric changed?)", e, CryptoException.Reason.KEY_PERMANENTLY_INVALIDATED
            )
            else -> CryptoException(
                e.message ?: "Unknown error", e, CryptoException.Reason.UNKNOWN
            )
        }
    }
}
