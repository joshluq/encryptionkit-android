package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.EncryptionkitConfigProvider
import es.joshluq.encryptionkit.domain.KeyRepository
import javax.inject.Inject

/**
 * Use case to initialize the library by ensuring the symmetric key exists.
 */
class InitializeLibraryUseCase @Inject constructor(
    private val keyRepository: KeyRepository,
    private val configProvider: EncryptionkitConfigProvider
) {
    operator fun invoke() {
        val config = configProvider.config
        if (!keyRepository.isKeyReady(config.alias)) {
            keyRepository.generateSymmetricKey(
                alias = config.alias,
                requireUserAuth = config.requireUserAuth,
                useStrongBox = config.useStrongBox
            )
        }
    }
}
