package es.joshluq.encryptionkit.data.repository

import android.content.Context
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import es.joshluq.encryptionkit.data.datasource.TinkDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.log.LoggerKit
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

internal class EncryptionRepositoryImpl(
    private val tinkDataSource: TinkDataSource,
    private val certificatePathProvider: CertificatePathProvider,
    private val logger: LoggerKit,
    private val context: Context
) : EncryptionRepository {

    companion object {
        private const val TAG = "EncryptionRepository"
    }

    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(alias: String) {
        logger.d(TAG, "Initializing key: $alias via Tink")
        tinkDataSource.getAead(alias)
    }

    override fun encryptSymmetric(data: ByteArray, alias: String, associatedData: ByteArray): CryptoResult {
        logger.d(TAG, "Encrypting symmetric data with alias: $alias using Tink")
        try {
            val aead = tinkDataSource.getAead(alias)
            val ciphertext = aead.encrypt(data, associatedData)
            return CryptoResult(ciphertext)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Symmetric encryption failed for alias: $alias", e)
            throw mapException(e)
        }
    }

    override fun decryptSymmetric(ciphertext: ByteArray, alias: String, associatedData: ByteArray): ByteArray {
        logger.d(TAG, "Decrypting symmetric data with alias: $alias using Tink")
        try {
            val aead = tinkDataSource.getAead(alias)
            return aead.decrypt(ciphertext, associatedData)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Symmetric decryption failed for alias: $alias", e)
            throw mapException(e)
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        logger.d(TAG, "Getting security level for Tink master key alias: $alias")
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = keyStore.getKey(alias, null) as? SecretKey ?: return SecurityLevel.SOFTWARE
            
            val factory = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (keyInfo.securityLevel) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> SecurityLevel.STRONGBOX
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> SecurityLevel.TRUSTED_ENVIRONMENT
                    else -> SecurityLevel.SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                if (keyInfo.isInsideSecureHardware) SecurityLevel.TRUSTED_ENVIRONMENT else SecurityLevel.SOFTWARE
            }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to determine security level", e)
            SecurityLevel.SOFTWARE
        }
    }

    override fun deleteKey(alias: String) {
        logger.d(TAG, "Deleting keyset and master key for alias: $alias")
        try {
            // Delete keyset from SharedPreferences
            context.getSharedPreferences("tink_prefs_$alias", Context.MODE_PRIVATE)
                .edit { clear() }
            
            // Delete master key from Android Keystore
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error deleting key $alias", e)
        }
    }

    override suspend fun getPublicKey(): PublicKey {
        logger.d(TAG, "Retrieving public key from certificate")
        val path = certificatePathProvider.getCertificatePath()
            ?: throw CryptoException(
                "Certificate path not provided by consumer",
                null,
                CryptoException.Reason.CERTIFICATE_NOT_FOUND
            )

        val file = File(path)
        if (!file.exists()) {
            throw CryptoException(
                "Certificate file not found at: $path",
                null,
                CryptoException.Reason.CERTIFICATE_NOT_FOUND
            )
        }

        return try {
            withContext(Dispatchers.IO) {
                FileInputStream(file).use { inputStream ->
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val certificate = certFactory.generateCertificate(inputStream)
                    certificate.publicKey
                }
            }
        } catch (e: CertificateException) {
            logger.e(TAG, "Failed to parse certificate", e)
            throw CryptoException(
                "Failed to parse certificate",
                e,
                CryptoException.Reason.OPERATION_FAILED
            )
        } catch (e: IOException) {
            logger.e(TAG, "Failed to read certificate file", e)
            throw CryptoException(
                "Failed to read certificate file",
                e,
                CryptoException.Reason.OPERATION_FAILED
            )
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

        return CryptoException(
            e.message ?: "Unknown error",
            e,
            CryptoException.Reason.UNKNOWN
        )
    }
}
