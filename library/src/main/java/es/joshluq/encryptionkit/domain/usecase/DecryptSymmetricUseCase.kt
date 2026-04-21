package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

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
        val config: EncryptionkitConfig
    ) : UseCaseInput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (!ciphertext.contentEquals(other.ciphertext)) return false
            if (!iv.contentEquals(other.iv)) return false
            if (config != other.config) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            result = 31 * result + config.hashCode()
            return result
        }
    }

    data class Output(val data: ByteArray) : UseCaseOutput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Output

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}
