package es.joshluq.encryptionkit.data.repository

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
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

    companion object {
        private const val LENGTH = 128
    }

    private val aesTransformation = "AES/GCM/NoPadding"
    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(config: EncryptionkitConfig) {
        try {
            keystoreDataSource.ensureKeyExists(
                config.alias,
                config.requireUserAuth,
                config.useStrongBox
            )
        } catch (e: GeneralSecurityException) {
            throw mapException(e)
        } catch (e: IOException) {
            throw mapException(e)
        }
    }

    override fun encryptSymmetric(data: ByteArray, alias: String): CryptoResult {
        try {
            val key = keystoreDataSource.getKey(alias)
                ?: throw CryptoException("Key not found", reason = CryptoException.Reason.KEY_NOT_FOUND)

            val cipher = Cipher.getInstance(aesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val ciphertext = cipher.doFinal(data)
            return CryptoResult(ciphertext, cipher.iv)
        } catch (e: GeneralSecurityException) {
            throw mapException(e)
        }
    }

    override fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, alias: String): ByteArray {
        try {
            val key = keystoreDataSource.getKey(alias)
                ?: throw CryptoException("Key not found", reason = CryptoException.Reason.KEY_NOT_FOUND)

            val cipher = Cipher.getInstance(aesTransformation)
            val spec = GCMParameterSpec(LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            return cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
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
        return try {
            fileDataSource.getPublicKeyFromCertificate()
        } catch (e: GeneralSecurityException) {
            throw mapException(e)
        } catch (e: IOException) {
            throw mapException(e)
        }
    }

    override suspend fun encryptAsymmetric(data: ByteArray, publicKeyHash: String): ByteArray {
        try {
            val publicKey = getPublicKey()

            val currentHash = hash(
                publicKey.encoded,
                "SHA-256"
            ).joinToString("") { "%02x".format(it) }

            if (!currentHash.equals(publicKeyHash, ignoreCase = true)) {
                throw CryptoException(
                    "Public key validation failed. Expected: $publicKeyHash, Found: $currentHash",
                    reason = CryptoException.Reason.PUBLIC_KEY_PINNING_FAILURE
                )
            }

            val cipher = Cipher.getInstance(rsaTransformation)
            val oaepParams = OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
            )
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(data)
        } catch (e: GeneralSecurityException) {
            throw mapException(e)
        }
    }

    override fun hash(data: ByteArray, algorithm: String): ByteArray {
        return try {
            MessageDigest.getInstance(algorithm).digest(data)
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(
                "Hash failed: algorithm $algorithm not found",
                e,
                CryptoException.Reason.OPERATION_FAILED
            )
        }
    }

    private fun mapException(e: Exception): CryptoException {
        if (e is CryptoException) return e

        return when (e) {
            is UserNotAuthenticatedException -> CryptoException(
                "User authentication required",
                e,
                CryptoException.Reason.USER_NOT_AUTHENTICATED
            )
            is KeyPermanentlyInvalidatedException -> CryptoException(
                "Key permanently invalidated (biometric changed?)",
                e,
                CryptoException.Reason.KEY_PERMANENTLY_INVALIDATED
            )
            else -> CryptoException(
                e.message ?: "Unknown error",
                e,
                CryptoException.Reason.UNKNOWN
            )
        }
    }
}
