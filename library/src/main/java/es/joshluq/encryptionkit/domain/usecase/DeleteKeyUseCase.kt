package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class DeleteKeyUseCase(
    private val repository: EncryptionRepository
) : UseCase<DeleteKeyUseCase.Input, NoneOutput> {

    override suspend fun invoke(input: Input): Result<NoneOutput> = runCatching {
        repository.deleteKey(input.alias)
        NoneOutput
    }

    data class Input(val alias: String) : UseCaseInput
}
