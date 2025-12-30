package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(data: ByteArray, config: EncryptionConfig): CryptoResult {
        return repository.encryptSymmetric(data, config)
    }

    operator fun invoke(secureData: SecureBytes, config: EncryptionConfig): CryptoResult {
        return repository.encryptSymmetric(secureData.data, config)
    }
}
