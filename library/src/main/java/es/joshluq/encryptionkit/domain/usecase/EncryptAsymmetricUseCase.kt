package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class EncryptAsymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptAsymmetricUseCase.Input, EncryptAsymmetricUseCase.Output> {

    override fun invoke(input: Input): Flow<Output> = flow {
        val result = if (input.secureData != null) {
            repository.encryptAsymmetric(input.secureData.data, input.config)
        } else {
            repository.encryptAsymmetric(input.data ?: byteArrayOf(), input.config)
        }
        emit(Output(result))
    }

    data class Input(
        val data: ByteArray? = null,
        val secureData: SecureBytes? = null,
        val config: EncryptionConfig
    ) : UseCaseInput

    data class Output(val data: ByteArray) : UseCaseOutput
}
