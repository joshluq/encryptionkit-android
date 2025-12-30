package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class DeleteKeyUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(alias: String) {
        repository.deleteKey(alias)
    }
}
