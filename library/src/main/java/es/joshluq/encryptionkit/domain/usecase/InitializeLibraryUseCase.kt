package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class InitializeLibraryUseCase(
    private val repository: EncryptionRepository
) : UseCase<InitializeLibraryUseCase.Input, NoneOutput> {

    override suspend fun invoke(input: Input): Result<NoneOutput> = runCatching {
        repository.initializeKey(input.config)
        NoneOutput
    }

    data class Input(val config: EncryptionConfig) : UseCaseInput
}
