package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(data: ByteArray, config: EncryptionConfig): CryptoResult {
        return repository.encryptSymmetric(data, config)
    }
}
