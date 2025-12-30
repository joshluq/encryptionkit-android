package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.EncryptionkitConfigProvider
import es.joshluq.encryptionkit.domain.KeyRepository
import es.joshluq.encryptionkit.domain.SecurityLevel
import javax.inject.Inject

/**
 * Use case to retrieve the security level of the current key.
 */
class GetSecurityLevelUseCase @Inject constructor(
    private val keyRepository: KeyRepository,
    private val configProvider: EncryptionkitConfigProvider
) {
    operator fun invoke(): SecurityLevel {
        return keyRepository.getSecurityLevel(configProvider.config.alias)
    }
}
