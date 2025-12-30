package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class DecryptSymmetricUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(ciphertext: ByteArray, iv: ByteArray, config: EncryptionConfig): ByteArray {
        return repository.decryptSymmetric(ciphertext, iv, config)
    }
}
