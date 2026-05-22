package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class DecryptSymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<DecryptSymmetricUseCase.Input, DecryptSymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.decryptSymmetric(input.ciphertext, input.alias, input.associatedData)
        Output(result)
    }

    data class Input(
        val ciphertext: ByteArray,
        val alias: String,
        val associatedData: ByteArray = ByteArray(0)
    ) : UseCaseInput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (!ciphertext.contentEquals(other.ciphertext)) return false
            if (alias != other.alias) return false
            if (!associatedData.contentEquals(other.associatedData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + alias.hashCode()
            result = 31 * result + associatedData.contentHashCode()
            return result
        }
    }

    data class Output(val data: ByteArray) : UseCaseOutput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Output
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}
