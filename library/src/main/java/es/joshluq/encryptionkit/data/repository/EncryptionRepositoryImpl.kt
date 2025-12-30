package es.joshluq.encryptionkit.data.repository

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

class EncryptionRepositoryImpl(
    private val keystoreDataSource: KeystoreDataSource,
    private val fileDataSource: FileDataSource
) : EncryptionRepository {

    private val aesTransformation = "AES/GCM/NoPadding"
    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(config: EncryptionConfig) {
        keystoreDataSource.ensureKeyExists(config)
    }

    override fun encryptSymmetric(data: ByteArray, config: EncryptionConfig): CryptoResult {
        try {
            val key = keystoreDataSource.getKey(config.alias) ?: throw CryptoException("Key not found", null, CryptoException.ErrorType.KEY_NOT_FOUND)
            val cipher = Cipher.getInstance(aesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return CryptoResult(cipher.doFinal(data), cipher.iv)
        } catch (e: Exception) {
            throw handleException(e)
        }
    }

    override fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, config: EncryptionConfig): ByteArray {
        try {
            val key = keystoreDataSource.getKey(config.alias) ?: throw CryptoException("Key not found", null, CryptoException.ErrorType.KEY_NOT_FOUND)
            val cipher = Cipher.getInstance(aesTransformation)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw handleException(e)
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        return keystoreDataSource.getSecurityLevel(alias)
    }

    override fun deleteKey(alias: String) {
        keystoreDataSource.deleteKey(alias)
    }

    override suspend fun getPublicKey(): PublicKey {
        return fileDataSource.getPublicKeyFromCertificate()
    }

    override suspend fun encryptAsymmetric(data: ByteArray): ByteArray {
        try {
            val publicKey = getPublicKey()
            val cipher = Cipher.getInstance(rsaTransformation)
            val oaepParams = OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
            )
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            throw handleException(e)
        }
    }

    override fun hash(data: ByteArray, algorithm: String): ByteArray {
        return try {
            MessageDigest.getInstance(algorithm).digest(data)
        } catch (e: Exception) {
            throw CryptoException("Hash failed", e, CryptoException.ErrorType.INVALID_ALGORITHM)
        }
    }

    private fun handleException(e: Exception): CryptoException {
        return e as? CryptoException
            ?: CryptoException(e.message ?: "Error", e, CryptoException.ErrorType.UNKNOWN)
    }
}
