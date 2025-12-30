package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class EncryptAsymmetricUseCase(
    private val repository: EncryptionRepository
) {
    suspend operator fun invoke(data: ByteArray): ByteArray {
        return repository.encryptAsymmetric(data)
    }
}
