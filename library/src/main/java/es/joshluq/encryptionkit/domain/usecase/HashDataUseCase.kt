package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class HashDataUseCase(
    private val repository: EncryptionRepository
) : UseCase<HashDataUseCase.Input, HashDataUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.hash(input.data, input.algorithm)
        Output(result)
    }

    data class Input(val data: ByteArray, val algorithm: String = "SHA-256") : UseCaseInput
    data class Output(val data: ByteArray) : UseCaseOutput
}
