package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class DecryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<DecryptSymmetricUseCase.Input, DecryptSymmetricUseCase.Output> {

    override fun invoke(input: Input): Flow<Output> = flow {
        val result = repository.decryptSymmetric(input.ciphertext, input.iv, input.config)
        emit(Output(result))
    }

    data class Input(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val config: EncryptionConfig
    ) : UseCaseInput

    data class Output(val data: ByteArray) : UseCaseOutput
}
