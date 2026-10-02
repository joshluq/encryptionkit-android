package es.joshluq.encryptionkit.domain.repository

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.PublicKey

internal interface EncryptionRepository {
    fun initializeKey(alias: String)

    fun encryptSymmetric(
        data: ByteArray,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    ): CryptoResult

    fun decryptSymmetric(
        ciphertext: ByteArray,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    ): ByteArray

    fun getSecurityLevel(alias: String): SecurityLevel

    fun deleteKey(alias: String)

    suspend fun getPublicKey(): PublicKey

    suspend fun encryptAsymmetric(
        data: ByteArray,
        publicKeyHash: String,
    ): ByteArray

    fun hash(
        data: ByteArray,
        algorithm: String,
    ): ByteArray

    fun encryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    )

    fun decryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    )

    fun encryptDeterministic(
        data: ByteArray,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    ): CryptoResult

    fun decryptDeterministic(
        ciphertext: ByteArray,
        alias: String,
        associatedData: ByteArray = ByteArray(0),
    ): ByteArray

    fun rotateKey(alias: String)

    fun sign(
        data: ByteArray,
        alias: String,
    ): ByteArray

    fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        alias: String,
    ): Boolean

    fun computeMac(
        data: ByteArray,
        alias: String,
    ): ByteArray

    fun verifyMac(
        data: ByteArray,
        mac: ByteArray,
        alias: String,
    ): Boolean
}
