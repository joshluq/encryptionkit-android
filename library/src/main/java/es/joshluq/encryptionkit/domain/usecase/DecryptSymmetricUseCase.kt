package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.CryptoEngine
import javax.inject.Inject

/**
 * Use case for symmetric decryption using AES-GCM.
 */
class DecryptSymmetricUseCase @Inject constructor(
    private val cryptoEngine: CryptoEngine
) {
    operator fun invoke(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        return cryptoEngine.decrypt(ciphertext, iv)
    }
}
