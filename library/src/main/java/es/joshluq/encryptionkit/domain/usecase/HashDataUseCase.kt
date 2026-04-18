package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class HashDataUseCase(
    private val repository: EncryptionRepository
) : UseCase<HashDataUseCase.Input, HashDataUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = repository.hash(input.data, input.algorithm)
        Output(result)
    }

    data class Input(val data: ByteArray, val algorithm: String = "SHA-256") : UseCaseInput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (!data.contentEquals(other.data)) return false
            if (algorithm != other.algorithm) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + algorithm.hashCode()
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
