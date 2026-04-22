package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class EncryptAsymmetricUseCase(
    private val repository: EncryptionRepository
) : UseCase<EncryptAsymmetricUseCase.Input, EncryptAsymmetricUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val result = if (input.secureData != null) {
            repository.encryptAsymmetric(input.secureData.data, input.publicKeyHash)
        } else {
            repository.encryptAsymmetric(input.data ?: byteArrayOf(), input.publicKeyHash)
        }
        Output(result)
    }

    data class Input(
        val data: ByteArray? = null,
        val secureData: SecureBytes? = null,
        val publicKeyHash: String
    ) : UseCaseInput {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (!data.contentEquals(other.data)) return false
            if (secureData != other.secureData) return false
            if (publicKeyHash != other.publicKeyHash) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data?.contentHashCode() ?: 0
            result = 31 * result + (secureData?.hashCode() ?: 0)
            result = 31 * result + publicKeyHash.hashCode()
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
