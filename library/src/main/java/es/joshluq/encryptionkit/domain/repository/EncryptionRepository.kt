package es.joshluq.encryptionkit.domain.repository

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.PublicKey

internal interface EncryptionRepository {
    fun initializeKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean)
    fun encryptSymmetric(data: ByteArray, alias: String): CryptoResult
    fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, alias: String): ByteArray
    fun getSecurityLevel(alias: String): SecurityLevel
    fun deleteKey(alias: String)
    suspend fun getPublicKey(): PublicKey
    suspend fun encryptAsymmetric(data: ByteArray, publicKeyHash: String): ByteArray
    fun hash(data: ByteArray, algorithm: String): ByteArray
}
