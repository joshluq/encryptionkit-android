package es.joshluq.encryptionkit.data.repository

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.log.Loggerkit
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
    private val fileDataSource: FileDataSource,
    private val logger: Loggerkit
) : EncryptionRepository {

    companion object {
        private const val LENGTH = 128
        private const val TAG = "EncryptionRepository"
    }

    private val aesTransformation = "AES/GCM/NoPadding"
    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean) {
        logger.d(TAG, "Initializing key: $alias (auth=$requireUserAuth, strongBox=$useStrongBox)")
        try {
            keystoreDataSource.ensureKeyExists(alias, requireUserAuth, useStrongBox)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Failed to initialize key: $alias", e)
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, "IO Error initializing key: $alias", e)
            throw mapException(e)
        }
    }

    override fun encryptSymmetric(data: ByteArray, alias: String): CryptoResult {
        logger.d(TAG, "Encrypting symmetric data with alias: $alias")
        try {
            val key = keystoreDataSource.getKey(alias)
                ?: throw CryptoException(
                    "Key not found",
                    reason = CryptoException.Reason.KEY_NOT_FOUND
                )

            val cipher = Cipher.getInstance(aesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val ciphertext = cipher.doFinal(data)
            return CryptoResult(ciphertext, cipher.iv)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Symmetric encryption failed for alias: $alias", e)
            throw mapException(e)
        }
    }

    override fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, alias: String): ByteArray {
        logger.d(TAG, "Decrypting symmetric data with alias: $alias")
        try {
            val key = keystoreDataSource.getKey(alias)
                ?: throw CryptoException(
                    "Key not found",
                    reason = CryptoException.Reason.KEY_NOT_FOUND
                )

            val cipher = Cipher.getInstance(aesTransformation)
            val spec = GCMParameterSpec(LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            return cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Symmetric decryption failed for alias: $alias", e)
            throw mapException(e)
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        logger.d(TAG, "Getting security level for alias: $alias")
        return keystoreDataSource.getSecurityLevel(alias)
    }

    override fun deleteKey(alias: String) {
        logger.d(TAG, "Deleting key with alias: $alias")
        keystoreDataSource.deleteKey(alias)
    }

    override suspend fun getPublicKey(): PublicKey {
        logger.d(TAG, "Retrieving public key from certificate")
        return try {
            fileDataSource.getPublicKeyFromCertificate()
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Security error retrieving public key", e)
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, "IO error retrieving public key", e)
            throw mapException(e)
        }
    }

    override suspend fun encryptAsymmetric(data: ByteArray, publicKeyHash: String): ByteArray {
        logger.d(TAG, "Encrypting asymmetric data. Verifying public key hash...")
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
            logger.e(TAG, "Asymmetric encryption failed", e)
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, "IO error during asymmetric encryption", e)
            throw mapException(e)
        }
    }

    override fun hash(data: ByteArray, algorithm: String): ByteArray {
        logger.d(TAG, "Hashing data with algorithm: $algorithm")
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
