package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class EncryptAsymmetricUseCase(
    private val repository: EncryptionRepository
) {
    suspend operator fun invoke(data: ByteArray, config: EncryptionConfig): ByteArray {
        return repository.encryptAsymmetric(data, config)
    }

    suspend operator fun invoke(secureData: SecureBytes, config: EncryptionConfig): ByteArray {
        return repository.encryptAsymmetric(secureData.data, config)
    }
}
