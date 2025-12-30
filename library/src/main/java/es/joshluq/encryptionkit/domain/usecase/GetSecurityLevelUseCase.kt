package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class GetSecurityLevelUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(alias: String): SecurityLevel {
        return repository.getSecurityLevel(alias)
    }
}
