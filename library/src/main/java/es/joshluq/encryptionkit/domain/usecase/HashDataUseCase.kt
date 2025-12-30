package es.joshluq.encryptionkit.domain.usecase

import java.security.MessageDigest
import javax.inject.Inject

/**
 * Use case for data hashing.
 * Supports SHA-256 (recommended) and MD5 (legacy/compatibility).
 */
class HashDataUseCase @Inject constructor() {
    
    operator fun invoke(data: ByteArray, algorithm: String = "SHA-256"): ByteArray {
        val digest = MessageDigest.getInstance(algorithm)
        return digest.digest(data)
    }

    fun toHexString(data: ByteArray, algorithm: String = "SHA-256"): String {
        return invoke(data, algorithm).joinToString("") { "%02x".format(it) }
    }
}
