package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class InitializeLibraryUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(config: EncryptionConfig) {
        repository.initializeKey(config)
    }
}
