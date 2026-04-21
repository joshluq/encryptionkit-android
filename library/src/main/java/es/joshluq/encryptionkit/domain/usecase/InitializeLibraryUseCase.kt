package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput

internal class InitializeLibraryUseCase(
    private val repository: EncryptionRepository
) : UseCase<InitializeLibraryUseCase.Input, NoneOutput> {

    override suspend fun invoke(input: Input): Result<NoneOutput> = runCatching {
        repository.initializeKey(input.config)
        NoneOutput
    }

    data class Input(val config: EncryptionkitConfig) : UseCaseInput
}
