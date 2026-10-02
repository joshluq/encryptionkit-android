package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

internal class DecryptStreamUseCase(
    private val repository: EncryptionRepository,
) : UseCase<DecryptStreamUseCase.Input, DecryptStreamUseCase.Output> {
    override suspend fun invoke(input: Input): Result<Output> =
        runCatching {
            withContext(Dispatchers.IO) {
                repository.decryptStream(input.inputStream, input.outputStream, input.alias, input.associatedData)
            }
            Output
        }

    data class Input(
        val inputStream: InputStream,
        val outputStream: OutputStream,
        val alias: String,
        val associatedData: ByteArray = ByteArray(0),
    ) : UseCaseInput {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Input
            if (inputStream != other.inputStream) return false
            if (outputStream != other.outputStream) return false
            if (alias != other.alias) return false
            if (!associatedData.contentEquals(other.associatedData)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = inputStream.hashCode()
            result = 31 * result + outputStream.hashCode()
            result = 31 * result + alias.hashCode()
            result = 31 * result + associatedData.contentHashCode()
            return result
        }
    }

    object Output : UseCaseOutput
}
