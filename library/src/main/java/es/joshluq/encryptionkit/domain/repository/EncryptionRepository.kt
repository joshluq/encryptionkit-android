package es.joshluq.encryptionkit.domain.repository

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.PublicKey

/**
 * Unified Repository Interface for Domain Layer.
 * Abstraction of all data operations.
 */
interface EncryptionRepository {
    // Symmetric Operations
    fun initializeKey(config: EncryptionConfig)
    fun encryptSymmetric(data: ByteArray, config: EncryptionConfig): CryptoResult
    fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, config: EncryptionConfig): ByteArray
    fun getSecurityLevel(alias: String): SecurityLevel
    fun deleteKey(alias: String)
    
    // Asymmetric Operations
    suspend fun getPublicKey(): PublicKey
    suspend fun encryptAsymmetric(data: ByteArray): ByteArray

    // Hashing
    fun hash(data: ByteArray, algorithm: String): ByteArray
}
