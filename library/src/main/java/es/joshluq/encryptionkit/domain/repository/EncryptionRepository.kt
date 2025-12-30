package es.joshluq.encryptionkit.domain.repository

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.PublicKey

internal interface EncryptionRepository {
    fun initializeKey(config: EncryptionConfig)
    fun encryptSymmetric(data: ByteArray, config: EncryptionConfig): CryptoResult
    fun decryptSymmetric(ciphertext: ByteArray, iv: ByteArray, config: EncryptionConfig): ByteArray
    fun getSecurityLevel(alias: String): SecurityLevel
    fun deleteKey(alias: String)
    
    suspend fun getPublicKey(): PublicKey
    suspend fun encryptAsymmetric(data: ByteArray, config: EncryptionConfig): ByteArray

    fun hash(data: ByteArray, algorithm: String): ByteArray
}
