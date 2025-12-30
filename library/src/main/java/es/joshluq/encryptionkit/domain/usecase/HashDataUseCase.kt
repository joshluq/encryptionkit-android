package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class HashDataUseCase(
    private val repository: EncryptionRepository
) : UseCase<HashDataUseCase.Input, HashDataUseCase.Output> {

    override fun invoke(input: Input): Flow<Output> = flow {
        val result = repository.hash(input.data, input.algorithm)
        emit(Output(result))
    }

    data class Input(val data: ByteArray, val algorithm: String = "SHA-256") : UseCaseInput
    data class Output(val data: ByteArray) : UseCaseOutput
}
