package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class DecryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<DecryptSymmetricUseCase.Input, DecryptSymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.decryptSymmetric(input.ciphertext, input.iv, input.config)
        Output(result)
    }

    data class Input(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val config: EncryptionConfig
    ) : UseCaseInput

    data class Output(val data: ByteArray) : UseCaseOutput
}
