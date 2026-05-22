package es.joshluq.encryptionkit.domain.repository

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.PublicKey

internal interface EncryptionRepository {
    fun initializeKey(alias: String)
    fun encryptSymmetric(data: ByteArray, alias: String, associatedData: ByteArray = ByteArray(0)): CryptoResult
    fun decryptSymmetric(ciphertext: ByteArray, alias: String, associatedData: ByteArray = ByteArray(0)): ByteArray
    fun getSecurityLevel(alias: String): SecurityLevel
    fun deleteKey(alias: String)
    suspend fun getPublicKey(): PublicKey
    suspend fun encryptAsymmetric(data: ByteArray, publicKeyHash: String): ByteArray
    fun hash(data: ByteArray, algorithm: String): ByteArray
}
