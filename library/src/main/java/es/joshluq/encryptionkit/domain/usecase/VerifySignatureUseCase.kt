package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class VerifySignatureUseCase(
    private val repository: EncryptionRepository,
) : UseCase<VerifySignatureUseCase.Input, VerifySignatureUseCase.Output> {
    override suspend fun invoke(input: Input): Result<Output> =
        runCatching {
            val isValid =
                withContext(Dispatchers.Default) {
                    repository.verifySignature(input.data, input.signature, input.alias)
                }
            Output(isValid)
        }

    data class Input(
        val data: ByteArray,
        val signature: ByteArray,
        val alias: String,
    ) : UseCaseInput {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Input
            if (!data.contentEquals(other.data)) return false
            if (!signature.contentEquals(other.signature)) return false
            if (alias != other.alias) return false
            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + signature.contentHashCode()
            result = 31 * result + alias.hashCode()
            return result
        }
    }

    data class Output(
        val isValid: Boolean,
    ) : UseCaseOutput
}
