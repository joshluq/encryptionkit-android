package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RotateKeyUseCase(
    private val repository: EncryptionRepository,
) : UseCase<RotateKeyUseCase.Input, NoneOutput> {
    override suspend fun invoke(input: Input): Result<NoneOutput> =
        runCatching {
            withContext(Dispatchers.IO) {
                repository.rotateKey(input.alias)
            }
            NoneOutput
        }

    data class Input(
        val alias: String,
    ) : UseCaseInput
}
