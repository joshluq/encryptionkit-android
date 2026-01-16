package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptSymmetricUseCase.Input, EncryptSymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.encryptSymmetric(input.data, input.config)
        Output(result)
    }

    data class Input(val data: ByteArray, val config: EncryptionConfig) : UseCaseInput
    data class Output(val result: CryptoResult) : UseCaseOutput
}
