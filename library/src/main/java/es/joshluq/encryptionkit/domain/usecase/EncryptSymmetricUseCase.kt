package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptSymmetricUseCase.Input, EncryptSymmetricUseCase.Output> {

    override fun invoke(input: Input): Flow<Output> = flow {
        val result = repository.encryptSymmetric(input.data, input.config)
        emit(Output(result))
    }

    data class Input(val data: ByteArray, val config: EncryptionConfig) : UseCaseInput
    data class Output(val result: CryptoResult) : UseCaseOutput
}
