package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class EncryptAsymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptAsymmetricUseCase.Input, EncryptAsymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = if (input.secureData != null) {
            repository.encryptAsymmetric(input.secureData.data, input.config)
        } else {
            repository.encryptAsymmetric(input.data ?: byteArrayOf(), input.config)
        }
        Output(result)
    }

    data class Input(
        val data: ByteArray? = null,
        val secureData: SecureBytes? = null,
        val config: EncryptionConfig
    ) : UseCaseInput

    data class Output(val data: ByteArray) : UseCaseOutput
}
