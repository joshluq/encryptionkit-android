package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.CryptoEngine
import es.joshluq.encryptionkit.domain.CryptoResult
import javax.inject.Inject

/**
 * Use case for symmetric encryption using AES-GCM.
 */
class EncryptSymmetricUseCase @Inject constructor(
    private val cryptoEngine: CryptoEngine
) {
    operator fun invoke(data: ByteArray): CryptoResult {
        return cryptoEngine.encrypt(data)
    }
}
