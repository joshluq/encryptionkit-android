package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SignDataUseCase(
    private val repository: EncryptionRepository,
) : UseCase<SignDataUseCase.Input, SignDataUseCase.Output> {
    override suspend fun invoke(input: Input): Result<Output> =
        runCatching {
            val signature =
                withContext(Dispatchers.Default) {
                    repository.sign(input.data, input.alias)
                }
            Output(signature)
        }

    data class Input(
        val data: ByteArray,
        val alias: String,
    ) : UseCaseInput {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Input
            if (!data.contentEquals(other.data)) return false
            if (alias != other.alias) return false
            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + alias.hashCode()
            return result
        }
    }

    data class Output(
        val signature: ByteArray,
    ) : UseCaseOutput {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Output
            return signature.contentEquals(other.signature)
        }

        override fun hashCode(): Int = signature.contentHashCode()
    }
}
