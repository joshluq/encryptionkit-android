package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.EncryptionkitConfigProvider
import es.joshluq.encryptionkit.domain.KeyRepository
import javax.inject.Inject

/**
 * Use case to delete the current key.
 */
class DeleteKeyUseCase @Inject constructor(
    private val keyRepository: KeyRepository,
    private val configProvider: EncryptionkitConfigProvider
) {
    operator fun invoke() {
        keyRepository.deleteSymmetricKey(configProvider.config.alias)
    }
}
