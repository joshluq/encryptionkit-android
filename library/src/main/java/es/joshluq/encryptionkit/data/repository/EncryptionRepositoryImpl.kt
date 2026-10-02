package es.joshluq.encryptionkit.data.repository

import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import es.joshluq.encryptionkit.data.datasource.TinkDataSource
import es.joshluq.encryptionkit.di.d
import es.joshluq.encryptionkit.di.e
import es.joshluq.encryptionkit.di.i
import es.joshluq.encryptionkit.di.w
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.log.LoggerKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

internal class EncryptionRepositoryImpl(
    private val tinkDataSource: TinkDataSource,
    private val certificatePathProvider: CertificatePathProvider,
    private val logger: LoggerKit,
) : EncryptionRepository {
    companion object {
        private const val TAG = "EncryptionRepository"
    }

    private val rsaTransformation = "RSA/ECB/OAEPPadding"

    override fun initializeKey(alias: String) {
        logger.d(TAG) { "Initializing key: $alias via Tink" }
        tinkDataSource.getAead(alias)
    }

    override fun encryptSymmetric(
        data: ByteArray,
        alias: String,
        associatedData: ByteArray,
    ): CryptoResult {
        logger.d(TAG) { "Encrypting symmetric data with alias: $alias using Tink" }
        try {
            val aead = tinkDataSource.getAead(alias)
            val ciphertext = aead.encrypt(data, associatedData)
            return CryptoResult(ciphertext)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Symmetric encryption failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun decryptSymmetric(
        ciphertext: ByteArray,
        alias: String,
        associatedData: ByteArray,
    ): ByteArray {
        logger.d(TAG) { "Decrypting symmetric data with alias: $alias using Tink" }
        try {
            val aead = tinkDataSource.getAead(alias)
            return aead.decrypt(ciphertext, associatedData)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Symmetric decryption failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        logger.d(TAG) { "Getting security level for Tink master key alias: $alias" }
        return runCatching {
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
        }.onFailure { e ->
            logger.e(TAG, e) { "Failed to determine security level" }
        }.getOrDefault(SecurityLevel.SOFTWARE)
    }

    override fun deleteKey(alias: String) {
        logger.d(TAG) { "Deleting keyset and master key for alias: $alias" }
        tinkDataSource.deleteAead(alias)
    }

    override suspend fun getPublicKey(): PublicKey {
        logger.d(TAG) { "Retrieving public key from certificate" }
        val path =
            certificatePathProvider.getCertificatePath()
                ?: throw CryptoException(
                    "Certificate path not provided by consumer",
                    null,
                    CryptoException.Reason.CERTIFICATE_NOT_FOUND,
                )

        val file = File(path)
        if (!file.exists()) {
            throw CryptoException(
                "Certificate file not found at: $path",
                null,
                CryptoException.Reason.CERTIFICATE_NOT_FOUND,
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
            logger.e(TAG, e) { "Failed to parse certificate" }
            throw CryptoException(
                "Failed to parse certificate",
                e,
                CryptoException.Reason.OPERATION_FAILED,
            )
        } catch (e: IOException) {
            logger.e(TAG, e) { "Failed to read certificate file" }
            throw CryptoException(
                "Failed to read certificate file",
                e,
                CryptoException.Reason.OPERATION_FAILED,
            )
        }
    }

    override suspend fun encryptAsymmetric(
        data: ByteArray,
        publicKeyHash: String,
    ): ByteArray {
        logger.d(TAG) { "Encrypting asymmetric data. Verifying public key hash..." }
        try {
            val publicKey = getPublicKey()

            val currentHash =
                hash(
                    publicKey.encoded,
                    "SHA-256",
                ).joinToString("") { "%02x".format(it) }

            if (!currentHash.equals(publicKeyHash, ignoreCase = true)) {
                throw CryptoException(
                    "Public key validation failed. Expected: $publicKeyHash, Found: $currentHash",
                    reason = CryptoException.Reason.PUBLIC_KEY_PINNING_FAILURE,
                )
            }

            val cipher = Cipher.getInstance(rsaTransformation)
            val oaepParams =
                OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT,
                )
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(data)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Asymmetric encryption failed" }
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error during asymmetric encryption" }
            throw mapException(e)
        }
    }

    override fun hash(
        data: ByteArray,
        algorithm: String,
    ): ByteArray {
        logger.d(TAG) { "Hashing data with algorithm: $algorithm" }
        return try {
            MessageDigest.getInstance(algorithm).digest(data)
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(
                "Hash failed: algorithm $algorithm not found",
                e,
                CryptoException.Reason.OPERATION_FAILED,
            )
        }
    }

    override fun encryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        alias: String,
        associatedData: ByteArray,
    ) {
        logger.d(TAG) { "Encrypting stream with alias: $alias using Tink StreamingAead" }
        try {
            val streamingAead = tinkDataSource.getStreamingAead(alias)
            streamingAead.newEncryptingStream(outputStream, associatedData).use { encryptingStream ->
                inputStream.copyTo(encryptingStream)
            }
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Streaming encryption failed for alias: $alias" }
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error during streaming encryption for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun decryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        alias: String,
        associatedData: ByteArray,
    ) {
        logger.d(TAG) { "Decrypting stream with alias: $alias using Tink StreamingAead" }
        try {
            val streamingAead = tinkDataSource.getStreamingAead(alias)
            streamingAead.newDecryptingStream(inputStream, associatedData).use { decryptingStream ->
                decryptingStream.copyTo(outputStream)
            }
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Streaming decryption failed for alias: $alias" }
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error during streaming decryption for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun encryptDeterministic(
        data: ByteArray,
        alias: String,
        associatedData: ByteArray,
    ): CryptoResult {
        logger.d(TAG) { "Encrypting deterministic data with alias: $alias using Tink DeterministicAead" }
        try {
            val daead = tinkDataSource.getDeterministicAead(alias)
            val ciphertext = daead.encryptDeterministically(data, associatedData)
            return CryptoResult(ciphertext)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Deterministic encryption failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun decryptDeterministic(
        ciphertext: ByteArray,
        alias: String,
        associatedData: ByteArray,
    ): ByteArray {
        logger.d(TAG) { "Decrypting deterministic data with alias: $alias using Tink DeterministicAead" }
        try {
            val daead = tinkDataSource.getDeterministicAead(alias)
            return daead.decryptDeterministically(ciphertext, associatedData)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Deterministic decryption failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun rotateKey(alias: String) {
        logger.i(TAG) { "Rotating key for alias: $alias" }
        try {
            tinkDataSource.rotateAead(alias)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Key rotation failed for alias: $alias" }
            throw mapException(e)
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error during key rotation for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun sign(
        data: ByteArray,
        alias: String,
    ): ByteArray {
        logger.d(TAG) { "Signing data with alias: $alias using Tink PublicKeySign" }
        try {
            val signer = tinkDataSource.getPublicKeySign(alias)
            return signer.sign(data)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Digital signature generation failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        alias: String,
    ): Boolean {
        logger.d(TAG) { "Verifying digital signature with alias: $alias using Tink PublicKeyVerify" }
        return try {
            val verifier = tinkDataSource.getPublicKeyVerify(alias)
            verifier.verify(signature, data)
            true
        } catch (e: GeneralSecurityException) {
            logger.w(TAG, e) { "Digital signature verification failed (invalid signature) for alias: $alias" }
            false
        }
    }

    override fun computeMac(
        data: ByteArray,
        alias: String,
    ): ByteArray {
        logger.d(TAG) { "Computing MAC with alias: $alias using Tink Mac" }
        try {
            val mac = tinkDataSource.getMac(alias)
            return mac.computeMac(data)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "MAC computation failed for alias: $alias" }
            throw mapException(e)
        }
    }

    override fun verifyMac(
        data: ByteArray,
        mac: ByteArray,
        alias: String,
    ): Boolean {
        logger.d(TAG) { "Verifying MAC with alias: $alias using Tink Mac" }
        return try {
            val macPrimitive = tinkDataSource.getMac(alias)
            macPrimitive.verifyMac(mac, data)
            true
        } catch (e: GeneralSecurityException) {
            logger.w(TAG, e) { "MAC verification failed (tag mismatch) for alias: $alias" }
            false
        }
    }

    private fun mapException(e: Exception): CryptoException {
        if (e is CryptoException) return e

        return CryptoException(
            e.message ?: "Unknown error",
            e,
            CryptoException.Reason.UNKNOWN,
        )
    }
}
