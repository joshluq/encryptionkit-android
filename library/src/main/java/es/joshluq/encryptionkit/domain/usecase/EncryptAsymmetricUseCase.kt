package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.AsymmetricEncryption
import es.joshluq.encryptionkit.domain.CryptoException
import es.joshluq.encryptionkit.domain.KeyRepository
import javax.inject.Inject

/**
 * Use case for asymmetric encryption using RSA-OAEP.
 * Orchestrates public key retrieval from repository and encryption.
 */
class EncryptAsymmetricUseCase @Inject constructor(
    private val keyRepository: KeyRepository,
    private val asymmetricEncryption: AsymmetricEncryption
) {
    suspend operator fun invoke(data: ByteArray): ByteArray {
        val publicKey = keyRepository.getPublicKey()
            ?: throw CryptoException(
                "Public key not found in repository",
                null,
                CryptoException.ErrorType.KEY_NOT_FOUND
            )
        
        return asymmetricEncryption.encrypt(data, publicKey)
    }
}
