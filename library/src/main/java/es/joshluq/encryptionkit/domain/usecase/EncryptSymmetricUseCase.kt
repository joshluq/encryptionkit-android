package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.encryptionkit.sdk.EncryptionConfig
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class EncryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptSymmetricUseCase.Input, EncryptSymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.encryptSymmetric(input.data, input.config)
        Output(result)
    }

    data class Input(val data: ByteArray, val config: EncryptionConfig) : UseCaseInput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (!data.contentEquals(other.data)) return false
            if (config != other.config) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + config.hashCode()
            return result
        }
    }

    data class Output(val result: CryptoResult) : UseCaseOutput
}
